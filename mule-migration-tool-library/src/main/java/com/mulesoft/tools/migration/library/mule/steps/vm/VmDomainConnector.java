/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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

    report.report("transports.domainConnector", vmCfg, vmCfg);
  }
}
