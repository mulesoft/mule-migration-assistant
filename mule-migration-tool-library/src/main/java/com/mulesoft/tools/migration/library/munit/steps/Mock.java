/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.munit.steps;

import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.attributeToChildNode;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeAttribute;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.moveAttributeToChildNode;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

import java.util.List;

/**
 * This steps migrates the MUnit 1.x mock
 * @author Mulesoft Inc.
 */
public class Mock extends AbstractApplicationModelMigrationStep {

  private static final String MOCK_NAMESPACE = "http://www.mulesoft.org/schema/mule/mock";
  private static final String MUNIT_TOOLS_PREFIX = "munit-tools";

  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + MOCK_NAMESPACE + "'"
      + " and local-name()='when']";

  @Override
  public String getDescription() {
    return "Update MUnit Mock component";
  }

  public Mock() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    try {
      changeNodeName(MUNIT_TOOLS_PREFIX, "mock-when")
          .andThen(changeAttribute("messageProcessor", of("processor"), empty()))
          .apply(element);

      updateChildElementsNamespace(element.getChildren());

      Element attributesNode = element.getChild("with-attributes", element.getNamespace());

      attributesNode.getChildren().forEach(n -> changeAttribute("name", of("attributeName"), empty())
          .apply(n));

      movePayloadToChildNode(element.getChild("then-return", element.getNamespace()));

    } catch (Exception e) {
      throw new MigrationStepException("Fail to apply step. " + e.getMessage());
    }
  }


  private void movePayloadToChildNode(Element element) {
    attributeToChildNode("payload").apply(element);
    moveAttributeToChildNode("encoding", "payload")
        .andThen(moveAttributeToChildNode("mimeType", "payload"))
        .apply(element);

    changeAttribute("mimeType", of("mediaType"), empty())
        .apply(element.getChild("payload", element.getNamespace()));
  }

  private void updateChildElementsNamespace(List<Element> childs) {
    childs.forEach(c -> {
      c.setNamespace(c.getDocument().getRootElement().getNamespace(MUNIT_TOOLS_PREFIX));
      updateChildElementsNamespace(c.getChildren());
    });

  }
}
