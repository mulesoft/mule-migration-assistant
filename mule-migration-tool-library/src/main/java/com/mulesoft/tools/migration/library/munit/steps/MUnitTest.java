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
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getXPathSelector;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.apache.commons.lang3.ArrayUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.ArrayList;
import java.util.List;

/**
 * This step migrates the MUnit test structure
 * @author Mulesoft Inc.
 */
public class MUnitTest extends AbstractApplicationModelMigrationStep {

  private static final String MUNIT_TOOLS_PREFIX = "munit-tools";
  private static final String MUNIT_TOOLS_URI = "http://www.mulesoft.org/schema/mule/munit-tools";
  private static final Namespace MUNIT_TOOLS_NS = Namespace.getNamespace(MUNIT_TOOLS_PREFIX, MUNIT_TOOLS_URI);
  private static final String MUNIT_NS_PREFIX = "munit";
  private static final String MUNIT_NS_URI = "http://www.mulesoft.org/schema/mule/munit";
  private static final Namespace MUNIT_NS = Namespace.getNamespace(MUNIT_NS_PREFIX, MUNIT_NS_URI);

  private static String[] sections = new String[] {"munit:behavior", "munit:execution", "munit:validation"};
  private static final String XPATH_SELECTOR = getXPathSelector("http://www.mulesoft.org/schema/mule/munit", "test", true);

  @Override
  public String getDescription() {
    return "Migrate MUnit test structure";
  }

  public MUnitTest() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(MUNIT_NS, MUNIT_TOOLS_NS));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    try {
      element.setAttribute("name", element.getAttributeValue("name")
          .replaceAll("/", "\\\\")
          .replaceAll("\\[|\\{", "(")
          .replaceAll("\\]|\\}", ")")
          .replaceAll("#", "_"));

      List<Element> childNodes = element.getChildren();
      createBehaviorSection(childNodes, element);
      createExecutionSection(childNodes, element);
      createValidationSection(childNodes, element);
    } catch (Exception e) {
      throw new MigrationStepException("Fail to apply step. " + e.getMessage());
    }
  }

  public void createBehaviorSection(List<Element> nodes, Element parent) {
    int pos = 0;
    List<Element> behaviorNodes = new ArrayList<>();
    while (nodes.size() > pos &&
        !nodes.get(pos).getQualifiedName().equals("flow-ref") &&
        !nodes.get(pos).getQualifiedName().equals("munit-tools:assert-that") &&
        !ArrayUtils.contains(sections, nodes.get(pos).getQualifiedName())) {
      behaviorNodes.add(nodes.get(pos));
      pos++;
    }
    createChildWithElements("behavior", behaviorNodes, parent);
  }

  public void createExecutionSection(List<Element> nodes, Element parent) {
    int pos = 0;
    List<Element> behaviorNodes = new ArrayList<>();
    while (nodes.size() > pos &&
        !nodes.get(pos).getQualifiedName().equals("munit-tools:assert-that") &&
        !ArrayUtils.contains(sections, nodes.get(pos).getQualifiedName())) {
      behaviorNodes.add(nodes.get(pos));
      pos++;
    }
    createChildWithElements("execution", behaviorNodes, parent);
  }

  public void createValidationSection(List<Element> nodes, Element parent) {
    int pos = 0;
    List<Element> behaviorNodes = new ArrayList<>();
    while (nodes.size() > pos &&
        !ArrayUtils.contains(sections, nodes.get(pos).getQualifiedName())) {
      behaviorNodes.add(nodes.get(pos));
      pos++;
    }
    createChildWithElements("validation", behaviorNodes, parent);
  }

  private void createChildWithElements(String childName, List<Element> elements, Element parent) {
    if (elements.size() > 0) {
      elements.forEach(parent::removeContent);
      Element section = new Element(childName);
      section.setNamespace(parent.getNamespace());
      section.addContent(elements);
      parent.addContent(section);
    }
  }
}
