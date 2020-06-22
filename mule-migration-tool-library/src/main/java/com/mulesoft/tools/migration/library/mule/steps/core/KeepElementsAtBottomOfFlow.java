/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementToBottom;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getContainerElement;
import static java.util.stream.Collectors.toList;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Ensure that elements with this attribute will remain at the bottom of the flow.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class KeepElementsAtBottomOfFlow extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "//*[@*[namespace-uri() = 'migration' and local-name()='lastElement']]";
  public static final Namespace MIGRATION_NAMESPACE = Namespace.getNamespace("migration");

  @Override
  public String getDescription() {
    return "Ensure that the element will remain at the bottom of the flow.";
  }

  public KeepElementsAtBottomOfFlow() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    element.getAttributes()
        .stream()
        .filter(att -> att.getNamespace().equals(MIGRATION_NAMESPACE) && att.getName().equals("lastElement"))
        .collect(toList())
        .forEach(att -> att.detach());
    element.removeNamespaceDeclaration(MIGRATION_NAMESPACE);

    Element flow = getContainerElement(element);
    element.detach();
    addElementToBottom(flow, element);
  }
}
