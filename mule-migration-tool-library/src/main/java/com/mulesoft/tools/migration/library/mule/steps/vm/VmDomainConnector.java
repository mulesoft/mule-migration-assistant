/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.vm;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the vm connector of the vm transport in a domain
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class VmDomainConnector extends VmConnector {

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    final Namespace vmConnectorNamespace = getNamespace("vm", "http://www.mulesoft.org/schema/mule/vm");
    getApplicationModel().addNameSpace(vmConnectorNamespace, "http://www.mulesoft.org/schema/mule/vm/current/mule-vm.xsd",
                                       object.getDocument());

    Element vmCfg = new Element("config", vmConnectorNamespace);
    vmCfg.setAttribute("name", object.getAttributeValue("name"));
    Element queues = new Element("queues", vmConnectorNamespace);
    vmCfg.addContent(queues);

    addTopLevelElement(vmCfg, object.getDocument());

    report.report("vm.domainConnector", vmCfg, vmCfg);
  }
}
