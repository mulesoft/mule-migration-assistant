/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.proxy;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.mulesoft.tools.migration.library.gateway.steps.GatewayMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

/**
 * Migrate DW WDSL properties
 *
 * @author Mulesoft Inc.
 */
public class DWWsdlPropertyAttributeValueMigrationStep extends GatewayMigrationStep {

  private static final String WSDL_PROPERTY_ATTRIBUTE_VALUE_START = "![wsdl(p[";

  private static final String HOST = "host";
  private static final String PATH = "path";
  private static final String PORT = "port";
  private static final String NAMESPACE = "namespace";
  private static final String SERVICE = "service";

  private static final String DW_WSDL_START = "#[Wsdl::";
  private static final String DW_WSDL_END = "}','${service.name}','${service.port}')]";

  private static final String ADDRESSES_0 = "addresses[0]";

  private static final String GET_PORT = "getPort";
  private static final String GET_HOST = "getHost";
  private static final String GET_PATH = "getPath";
  private static final String NAME = "name";


  public DWWsdlPropertyAttributeValueMigrationStep() {
    this.setAppliedTo(getXPathSelector(WSDL_PROPERTY_ATTRIBUTE_VALUE_START));
  }

  private String extractWsdlLocationProperty(String attributeValue) {
    return attributeValue.substring(10, attributeValue.indexOf("']"));
  }

  private void setFunctionAttributeValue(Element element, MigrationReport migrationReport, String attributeName,
                                         String mule3AttributeValue, String getValue) {
    element.setAttribute(attributeName, new StringBuilder(DW_WSDL_START).append(getValue).append("('${")
        .append(extractWsdlLocationProperty(mule3AttributeValue)).append(DW_WSDL_END).toString());
    migrationReport.report("proxy.functionWsdlPropertyAttribute", element, element, mule3AttributeValue);
  }

  private String getPropertyAttributeValue(String value) {
    return "service." + value;
  }

  private void setAttributeValue(Element element, MigrationReport migrationReport, String attributeName,
                                 String mule3AttributeValue, String mule4AttributeValue) {
    element.setAttribute(attributeName, "${" + mule4AttributeValue + "}");
    migrationReport.report("proxy.wsdlPropertyAttribute", element, element, mule3AttributeValue, mule4AttributeValue);
  }

  private void replaceValue(Element element, MigrationReport migrationReport, Attribute attribute) {
    String name = attribute.getName();
    switch (name) {
      case HOST:
        setFunctionAttributeValue(element, migrationReport, HOST, element.getAttributeValue(HOST), GET_HOST);
        break;
      case PORT:
        if (attribute.getValue().contains(ADDRESSES_0)) {
          setFunctionAttributeValue(element, migrationReport, PORT, element.getAttributeValue(PORT), GET_PORT);
        } else {
          setAttributeValue(element, migrationReport, PORT, attribute.getValue(), getPropertyAttributeValue(PORT));
        }
        break;
      case PATH:
        setFunctionAttributeValue(element, migrationReport, PATH, element.getAttributeValue(PATH), GET_PATH);
        break;
      case NAMESPACE:
        setAttributeValue(element, migrationReport, NAMESPACE, attribute.getValue(), getPropertyAttributeValue(NAMESPACE));
        break;
      case SERVICE:
        setAttributeValue(element, migrationReport, SERVICE, attribute.getValue(), getPropertyAttributeValue(NAME));
        break;
      default:
        migrationReport.report("proxy.unknownAttributeValue", attribute.getParent(), attribute.getParent(), name);
    }
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    new ProxyPomContributionMigrationStep(false).execute(getApplicationModel().getPomModel().get(), migrationReport);
    element.getAttributes().stream()
        .filter(attr -> attr.getValue().startsWith(WSDL_PROPERTY_ATTRIBUTE_VALUE_START))
        .forEach(attribute -> replaceValue(element, migrationReport, attribute));
  }
}
