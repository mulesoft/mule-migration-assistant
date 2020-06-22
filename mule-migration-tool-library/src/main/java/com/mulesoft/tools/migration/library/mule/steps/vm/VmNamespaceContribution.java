/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.vm;

import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.VM_NAMESPACE_PREFIX;
import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.VM_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.VM_SCHEMA_LOCATION;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.NamespaceContribution;

/**
 * Handles the addition of the vm namespace to the xml being migrated.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class VmNamespaceContribution implements NamespaceContribution {

  @Override
  public String getDescription() {
    return "Add VM namespace";
  }

  @Override
  public void execute(ApplicationModel object, MigrationReport report) throws RuntimeException {
    object.addNameSpace(VM_NAMESPACE_PREFIX, VM_NAMESPACE_URI, VM_SCHEMA_LOCATION);
  }
}
