package com.mulesoft.tools.migration.library.mule.steps.splitter;

import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.VM_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

public class VmConfig extends AbstractApplicationModelMigrationStep {

  private static final String XPATH_SELECTOR = "/*"; //Root element, should only be called once.

  private static final Element VM_CONFIG_TEMPLATE = new Element("config", VM_NAMESPACE);
  private static final Element VM_QUEUES_TEMPLATE = new Element("queues", VM_NAMESPACE);
  private static final Element VM_QUEUE_TEMPLATE = new Element("queue", VM_NAMESPACE);

  private VmInformation vmInformation;

  public VmConfig(VmInformation vmInformation) {
    this.setAppliedTo(XPATH_SELECTOR);
    this.vmInformation = vmInformation;
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    if(!vmInformation.getQueues().isEmpty()) {
      Element vmConfig = VM_CONFIG_TEMPLATE.clone();
      vmConfig.setAttribute("name", vmInformation.getConfigurationName());

      Element vmQueues = VM_QUEUES_TEMPLATE.clone();
      for (String queue : vmInformation.getQueues()) {
        vmQueues.addContent(VM_QUEUE_TEMPLATE.clone().setAttribute("name", queue));
      }
      vmConfig.addContent(vmQueues);
      addTopLevelElement(vmConfig, object.getDocument());
    }
  }
}
