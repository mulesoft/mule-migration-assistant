/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementToBottom;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getTopLevelCoreXPathSelector;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.isErrorHanldingElement;
import static java.util.Collections.reverse;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;

/**
 * Migrate flow definitions
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Flow extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = getTopLevelCoreXPathSelector("flow");


  @Override
  public String getDescription() {
    return "Migrate flow definitions";
  }

  public Flow() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    element.setAttribute("name", element.getAttributeValue("name")
        .replaceAll("/", "\\\\")
        .replaceAll("\\[|\\{", "(")
        .replaceAll("\\]|\\}", ")")
        .replaceAll("#", "_"));

    Attribute processingStrategy = element.getAttribute("processingStrategy");
    if (processingStrategy != null) {
      if ("synchronous".equals(processingStrategy.getValue())) {
        element.setAttribute("maxConcurrency", "1");
      } else {
        Element processingStrategyConfig = getApplicationModel().getNode("//*[@name = '" + processingStrategy.getValue() + "']");
        if (processingStrategyConfig != null) {
          processingStrategyConfig.detach();
          if (processingStrategyConfig.getAttribute("maxThreads") != null) {
            element.setAttribute("maxConcurrency", processingStrategyConfig.getAttribute("maxThreads").getValue());
          }
        }
      }

      element.removeAttribute(processingStrategy);
      report.report("flow.processingStrategy", element, element);
    }

    List<Element> responses = new ArrayList<>(element.getChildren("response", CORE_NAMESPACE));
    reverse(responses);

    for (Element response : responses) {
      Element wrappingTry = new Element("try", CORE_NAMESPACE);

      new ArrayList<>(element.getContent().subList(element.indexOf(response) + 1, element.getContentSize())).forEach(c -> {
        if (c instanceof Element && !isErrorHanldingElement((Element) c)) {
          c.detach();
          wrappingTry.addContent(c);
        }
      });

      addElementAfter(wrappingTry, response);

      List<Content> contents = response.cloneContent();
      response.detach();
      for (Content content : contents) {
        // error handlers are always last
        addElementToBottom(element, content);
      }
    }
  }

}
