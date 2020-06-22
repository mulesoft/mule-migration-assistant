/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.vm;

import static com.google.common.collect.Lists.newArrayList;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the vm connector of the vm transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class VmConnector extends AbstractApplicationModelMigrationStep {

  private static final String VM_NAMESPACE_PREFIX = "vm";
  private static final String VM_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/vm";
  private static final Namespace VM_NAMESPACE = Namespace.getNamespace(VM_NAMESPACE_PREFIX, VM_NAMESPACE_URI);

  public static final String XPATH_SELECTOR =
      "/*/*[namespace-uri() = '" + VM_NAMESPACE_URI + "' and local-name() = 'connector']";

  @Override
  public String getDescription() {
    return "Update vm connector config.";
  }

  public VmConnector() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(VM_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.detach();
  }
}
