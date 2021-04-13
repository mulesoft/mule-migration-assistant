/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.munit.steps;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getXPathSelector;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;

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
