/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.jms;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the jms connector of the JMS transport in a domain
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class JmsDomainConnector extends JmsConnector {

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    final Namespace jmsConnectorNamespace = getNamespace("jms", "http://www.mulesoft.org/schema/mule/jms");
    getApplicationModel().addNameSpace(jmsConnectorNamespace, "http://www.mulesoft.org/schema/mule/jms/current/mule-jms.xsd",
                                       object.getDocument());

    final Element jmsCfg = new Element("config", jmsConnectorNamespace);
    jmsCfg.setAttribute("name", object.getAttributeValue("name"));

    AbstractJmsEndpoint.addConnectionToConfig(jmsCfg, object, getApplicationModel(), report);

    addTopLevelElement(jmsCfg, object.getDocument());
  }
}
