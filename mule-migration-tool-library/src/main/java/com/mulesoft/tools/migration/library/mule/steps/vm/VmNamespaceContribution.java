/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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
