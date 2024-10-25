/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.ipfilter;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.IP_FILTER_GW_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.IP_FILTER_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.policy.ipfilter.AbstractIpFilterMigrationStep.CONFIG_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.steps.policy.ipfilter.AbstractIpFilterMigrationStep.IPS_TAG_NAME;

import com.mulesoft.tools.migration.library.gateway.steps.policy.FilterTagMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.List;

import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Parent;

/**
 * Migrate ip-filter element
 *
 * @author Mulesoft Inc.
 */
public class IpFilterTagMigrationStep extends FilterTagMigrationStep {

  private static final String FILTER_TAG_NAME = "filter";
  private static final String ON_UNACCEPTED_ATTR_NAME = "onUnaccepted";

  private static final String IP_FILTER_XSI_SCHEMA_LOCATION_URI_MULE3 = "http://www.mulesoft.org/schema/mule/ip-filter-gw";
  private static final String IP_FILTER_XSI_SCHEMA_LOCATION_URI_MULE3_XSD =
      "http://www.mulesoft.org/schema/mule/ip-filter-gw/current/mule-ip-filter-gw.xsd";
  private static final String IP_FILTER_XSI_SCHEMA_LOCATION_URI_MULE4 = "http://www.mulesoft.org/schema/mule/ip";
  private static final String IP_FILTER_XSI_SCHEMA_LOCATION_URI_MULE4_XSD =
      "http://www.mulesoft.org/schema/mule/ip/current/mule-ip.xsd";

  public IpFilterTagMigrationStep() {
    super(IP_FILTER_GW_NAMESPACE, FILTER_TAG_NAME);
  }

  @Override
  protected void migrateProcessorChain(Element root, String onUnacceptedName, MigrationReport migrationReport) {
    IpFilterProcessorChainTagMigrationStep step = new IpFilterProcessorChainTagMigrationStep(onUnacceptedName);
    step.setApplicationModel(getApplicationModel());
    step.execute(getProcessorChain(root, onUnacceptedName),
                 migrationReport);
  }

  private void moveElement(Element element) {
    final List<Content> content = detachContent(element.getContent());
    Element root = getRootElement(element);
    Parent parent = element.getParent();
    if (root != null && parent != null) {
      parent.addContent(0, content);
      parent.removeContent(element);
      root.addContent(element);
    }
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    migrateRootElement(element, IP_FILTER_GW_NAMESPACE, IP_FILTER_NAMESPACE, IP_FILTER_XSI_SCHEMA_LOCATION_URI_MULE3_XSD,
                       IP_FILTER_XSI_SCHEMA_LOCATION_URI_MULE4_XSD, IP_FILTER_XSI_SCHEMA_LOCATION_URI_MULE3,
                       IP_FILTER_XSI_SCHEMA_LOCATION_URI_MULE4);
    Element root = getRootElement(element);
    final String onUnacceptedName = element.getAttributeValue(ON_UNACCEPTED_ATTR_NAME);
    if (root != null && hasProcessorChain(root, onUnacceptedName)) {
      migrateProcessorChain(root, onUnacceptedName, migrationReport);
    }
    element.setName(CONFIG_TAG_NAME);
    element.setNamespace(IP_FILTER_NAMESPACE);
    moveElement(element);
    element.addContent(new Element(IPS_TAG_NAME, IP_FILTER_NAMESPACE));
    element.removeAttribute(ON_UNACCEPTED_ATTR_NAME);
  }
}
