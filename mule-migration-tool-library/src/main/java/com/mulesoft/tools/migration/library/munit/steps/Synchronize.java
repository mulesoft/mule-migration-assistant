/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.munit.steps;

import static com.google.common.collect.Lists.newArrayList;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.ArrayList;
import java.util.List;

/**
 * Migrate MUnit Synchronize
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Synchronize extends AbstractApplicationModelMigrationStep {

  private static final String SYNCHRONIZE_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/synchronize";
  private static final String SYNCRHONIZE_NAMESPACE_PREFIX = "synchronize";
  private static final Namespace SYNCRHONIZE_NAMESPACE =
      Namespace.getNamespace(SYNCRHONIZE_NAMESPACE_PREFIX, SYNCHRONIZE_NAMESPACE_URI);

  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + SYNCHRONIZE_NAMESPACE_URI + "'"
      + " and local-name()='run-and-wait']";

  @Override
  public String getDescription() {
    return "Update MUnit Mock component";
  }

  public Synchronize() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(SYNCRHONIZE_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    Element parent = element.getParentElement();
    Integer position = parent.indexOf(element);

    List<Element> childNodes = new ArrayList<>(element.getChildren());

    for (Element child : childNodes) {
      child.detach();
      parent.addContent(position, child);
      position++;
    }

    element.detach();
  }
}
