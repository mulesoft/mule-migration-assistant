/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.munit.steps;

import static com.google.common.collect.Lists.newArrayList;
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
import org.jdom2.Namespace;

import java.util.List;

/**
 * This steps migrates the MUnit 1.x mock
 * @author Mulesoft Inc.
 */
public class Mock extends AbstractApplicationModelMigrationStep {

  private static final String MOCK_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/mock";
  private static final String MOCK_NAMESPACE_PREFIX = "mock";
  private static final Namespace MOCK_NAMESPACE = Namespace.getNamespace(MOCK_NAMESPACE_PREFIX, MOCK_NAMESPACE_URI);
  private static final String MUNIT_TOOLS_PREFIX = "munit-tools";

  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + MOCK_NAMESPACE_URI + "'"
      + " and local-name()='when']";

  @Override
  public String getDescription() {
    return "Update MUnit Mock component";
  }

  public Mock() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(MOCK_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    try {
      changeNodeName(MUNIT_TOOLS_PREFIX, "mock-when")
          .andThen(changeAttribute("messageProcessor", of("processor"), empty()))
          .apply(element);

      updateChildElementsNamespace(element.getChildren());

      Element attributesNode = element.getChild("with-attributes", element.getNamespace());
      if (attributesNode != null) {
        attributesNode.getChildren().forEach(n -> changeAttribute("name", of("attributeName"), empty())
            .apply(n));
      }

      Element thenReturnNode = element.getChild("then-return", element.getNamespace());
      if (thenReturnNode != null) {
        movePayloadToChildNode(thenReturnNode);
      }

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
