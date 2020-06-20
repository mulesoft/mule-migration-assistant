/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
