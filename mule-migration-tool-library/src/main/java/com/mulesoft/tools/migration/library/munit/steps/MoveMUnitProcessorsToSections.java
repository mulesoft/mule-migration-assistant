/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.munit.steps;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import org.apache.commons.lang3.ArrayUtils;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * This steps migrates the MUnit 1.x assert-true
 * @author Mulesoft Inc.
 */
public class MoveMUnitProcessorsToSections extends AbstractApplicationModelMigrationStep {

  private static String[] sections = new String[] {"munit:behavior", "munit:execution", "munit:validation"};
  public static final String XPATH_SELECTOR = "//*[local-name()='test']";

  @Override
  public String getDescription() {
    return "Update Assert True to new MUnit Assertion component";
  }

  public MoveMUnitProcessorsToSections() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element) throws RuntimeException {
    try {
      List<Element> childNodes = element.getChildren();
      CreateBehaviorSection(childNodes, element);
      CreateExecutionSection(childNodes, element);
      CreateValidationSection(childNodes, element);
    } catch (Exception e) {
      throw new MigrationStepException("Fail to apply step. " + e.getMessage());
    }
  }

  public void CreateBehaviorSection(List<Element> nodes, Element parent) {
    int pos = 0;
    List<Element> behaviorNodes = new ArrayList<>();
    while (nodes.size() > pos &&
        !nodes.get(pos).getQualifiedName().equals("flow-ref") &&
        !nodes.get(pos).getQualifiedName().equals("munit-tools:assert-that") &&
        !ArrayUtils.contains(sections, nodes.get(pos).getQualifiedName())) {
      behaviorNodes.add(nodes.get(pos));
      pos++;
    }
    CreateChildWithElements("behavior", behaviorNodes, parent);
  }

  public void CreateExecutionSection(List<Element> nodes, Element parent) {
    int pos = 0;
    List<Element> behaviorNodes = new ArrayList<>();
    while (nodes.size() > pos &&
        !nodes.get(pos).getQualifiedName().equals("munit-tools:assert-that") &&
        !ArrayUtils.contains(sections, nodes.get(pos).getQualifiedName())) {
      behaviorNodes.add(nodes.get(pos));
      pos++;
    }
    CreateChildWithElements("execution", behaviorNodes, parent);
  }

  public void CreateValidationSection(List<Element> nodes, Element parent) {
    int pos = 0;
    List<Element> behaviorNodes = new ArrayList<>();
    while (nodes.size() > pos &&
        !ArrayUtils.contains(sections, nodes.get(pos).getQualifiedName())) {
      behaviorNodes.add(nodes.get(pos));
      pos++;
    }
    CreateChildWithElements("validation", behaviorNodes, parent);
  }

  private void CreateChildWithElements(String childName, List<Element> elements, Element parent) {
    if (elements.size() > 0) {
      elements.forEach(parent::removeContent);
      Element section = new Element(childName);
      section.setNamespace(parent.getNamespace());
      section.addContent(elements);
      parent.addContent(section);
    }
  }
}
