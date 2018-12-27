package com.mulesoft.tools.migration.library.mule.steps.vm;

import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.VM_NAMESPACE_PREFIX;
import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.VM_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.VM_SCHEMA_LOCATION;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.NamespaceContribution;

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
