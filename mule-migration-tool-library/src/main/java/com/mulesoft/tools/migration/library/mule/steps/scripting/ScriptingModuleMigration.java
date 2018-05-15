/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.scripting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Attribute;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;

/**
 * Update scripting module.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ScriptingModuleMigration extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "//scripting:*[local-name()='component']";

  public ScriptingModuleMigration() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public String getDescription() {
    return "Update scripting module.";
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    changeNodeName("scripting", "execute").apply(element);

    Element scriptNode = element.getChildren("script", element.getNamespace()).size() > 0
        ? element.getChildren("script", element.getNamespace()).get(0)
        : null;
    if (scriptNode != null) {
      changeNodeName("scripting", "code").apply(scriptNode);
      Attribute attribute = scriptNode.getAttribute("engine");
      if (attribute != null) {
        attribute.setValue(updateEngineValue(attribute.getValue()).toLowerCase());
        scriptNode.removeAttribute(attribute);
        element.setAttribute(attribute);
      } else {
        element.setAttribute("engine", "groovy");
      }
      attribute = scriptNode.getAttribute("file");
      if (attribute != null) {
        scriptNode.addContent("${file::" + attribute.getValue() + "}");
        scriptNode.removeAttribute(attribute);
      }
      movePropertiesToMap(scriptNode);
    }
    report.report(ERROR, element, element, "The message format in Mule 4 has changed. Please review the docs about it",
                  "https://docs.mulesoft.com/mule4-user-guide/v/4.1/intro-mule-message",
                  "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-module-scripting");
  }

  private String updateEngineValue(String engine) {
    if (engine.equalsIgnoreCase("jruby")) {
      return "ruby";
    } else if (engine.equalsIgnoreCase("javascript")) {
      return "nashorn";
    } else {
      return engine;
    }
  }

  private void movePropertiesToMap(Element scriptNode) {
    Map<String, String> scriptParameters = new HashMap<>();
    List<Element> childsToRemove = new ArrayList<>();
    if (scriptNode.getChildren().size() > 0) {
      scriptNode.getChildren().forEach(p -> {
        scriptParameters.put(p.getAttributeValue("key"), p.getAttributeValue("value"));
        childsToRemove.add(p);
      });
      childsToRemove.forEach(s -> s.getParent().removeContent(s));

      Element parametersElement = new Element("parameters", scriptNode.getNamespace());

      Gson gson = new GsonBuilder().create();
      String jsonMap = gson.toJson(scriptParameters);

      parametersElement.addContent("#[" + jsonMap + "]");
      scriptNode.getParentElement().addContent(parametersElement);
    }
  }
}
