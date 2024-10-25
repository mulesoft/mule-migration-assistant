/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.ipfilter;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.CONFIG;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.ERROR_HANDLER_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.EXECUTE_NEXT_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.ON_ERROR_CONTINUE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.ON_UNACCEPTED;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.PROXY_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.SET_PAYLOAD_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.SET_PROPERTY_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.SOURCE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.TRY_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_POLICY_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.IP_FILTER_GW_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.IP_FILTER_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_CORE_NAMESPACE_NO_PREFIX;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_POLICY_NAMESPACE;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.mulesoft.tools.migration.library.gateway.steps.policy.ipfilter.IpFilterTagMigrationStep;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;

public class IpFilterTagMigrationStepTestCase extends AbstractIpFilterMigrationTestCase {

  private static final String FILTER_TAG_NAME = "filter";
  private static final String IPS_TAG_NAME = "ips";
  private static final String SUB_FLOW_TAG_NAME = "sub-flow";
  private static final String NAME_ATTR_NAME = "name";
  private static final String ON_UNACCEPTED_ATTR_VALUE = "{{policyId}}-build-response";
  private static final String PROPERTY_NAME_ATTR_NAME = "propertyName";
  private static final String TYPE_ATTR_NAME = "type";
  private static final String LOG_EXCEPTION_ATTR_NAME = "logException";
  private static final String HTTP_STATUS = "http.status";
  private static final String CONTENT_TYPE = "Content-Type";
  private static final String VALUE_ATTR_NAME = "value";
  private static final String VALUE_403 = "403";
  private static final String VALUE_APP_XML = "application/xml";
  private static final String VALUE_APP_JSON = "application/json";
  private static final String VALUE_SOAP_FAULT = "#[soapFault('client', flowVars._ipViolationMessage)]";
  private static final String VALUE_FLOW_VARS = "#[flowVars._ipViolationMessage]";
  private static final String IF_WSDL_OPEN = "{{#isWsdlEndpoint}}";
  private static final String IF_WSDL_CLOSE = "{{/isWsdlEndpoint}}";
  private static final String IF_NOT_WSDL_OPEN = "{{^isWsdlEndpoint}}";
  private static final String IP_REJECTED = "IP:REJECTED";
  private static final String FALSE = "false";

  @Override
  protected Element getTestElement() {
    return new Element(FILTER_TAG_NAME, IP_FILTER_GW_NAMESPACE)
        .setAttribute(new Attribute(IP_ADDRESS_ATTR_NAME, IP_ADDRESS_ATTR_VALUE))
        .setAttribute(new Attribute(ON_UNACCEPTED, ON_UNACCEPTED_ATTR_VALUE));
  }

  private Element getSetPropertyElement(String propertyAttrValue, String valueAttrValue) {
    return new Element(SET_PROPERTY_TAG_NAME, MULE_4_POLICY_NAMESPACE)
        .setAttribute(new Attribute(PROPERTY_NAME_ATTR_NAME, propertyAttrValue))
        .setAttribute(new Attribute(VALUE_ATTR_NAME, valueAttrValue));
  }

  private Element getProcessorChainElement() {
    return new Element(SUB_FLOW_TAG_NAME, MULE_4_POLICY_NAMESPACE)
        .setAttribute(new Attribute(NAME_ATTR_NAME, ON_UNACCEPTED_ATTR_VALUE))
        .addContent(getSetPropertyElement(HTTP_STATUS, VALUE_403))
        .addContent(IF_WSDL_OPEN)
        .addContent(getSetPropertyElement(CONTENT_TYPE, VALUE_APP_XML))
        .addContent(new Element(SET_PAYLOAD_TAG_NAME, MULE_4_POLICY_NAMESPACE)
            .setAttribute(new Attribute(VALUE_ATTR_NAME, VALUE_SOAP_FAULT)))
        .addContent(IF_WSDL_CLOSE)
        .addContent(IF_NOT_WSDL_OPEN)
        .addContent(getSetPropertyElement(CONTENT_TYPE, VALUE_APP_JSON))
        .addContent(new Element(SET_PAYLOAD_TAG_NAME, MULE_4_POLICY_NAMESPACE)
            .setAttribute(new Attribute(VALUE_ATTR_NAME, VALUE_FLOW_VARS)))
        .addContent(IF_WSDL_CLOSE);
  }

  private void addProcessorChainElement(Element element) {
    Element root = new Element(MULE_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
    root.addNamespaceDeclaration(MULE_4_POLICY_NAMESPACE);
    root.addContent(getProcessorChainElement()).addContent(element);
    new Document().setRootElement(root);
  }

  private void assertFilterTag(Element element) {
    assertThat(element.getName(), is(CONFIG));
    assertThat(element.getNamespace(), is(IP_FILTER_NAMESPACE));
    assertThat(element.getAttribute(IP_ADDRESS_ATTR_NAME).getValue(), is(IP_ADDRESS_ATTR_VALUE));
    assertThat(element.getAttributes().size(), is(1));
    assertThat(element.getContentSize(), is(1));
    Element ipsElement = (Element) element.getContent(0);
    assertThat(ipsElement.getName(), is(IPS_TAG_NAME));
    assertThat(ipsElement.getNamespace(), is(IP_FILTER_NAMESPACE));
  }

  private void assertDwlScript() {
    String[] resourcesFiles = appModel.getProjectBasePath().resolve(MIGRATION_RESOURCES_PATH).toFile().list();
    assertThat(requireNonNull(resourcesFiles).length, is(1));
    assertThat(requireNonNull(resourcesFiles)[0], is("HttpListener.dwl"));
  }

  private Element getRootElement(final Element element) {
    Document doc = element.getDocument();
    if (doc != null) {
      return doc.getRootElement();
    }
    return null;
  }

  private void assertProcessorChain(Element element) {
    List<Element> setPropertyElements = element.getChildren(SET_PROPERTY_TAG_NAME, MULE_4_POLICY_NAMESPACE);
    assertThat(setPropertyElements, notNullValue());
    assertThat(setPropertyElements.size(), is(3));
    assertThat(setPropertyElements.get(0).getAttributeValue(PROPERTY_NAME_ATTR_NAME), is(HTTP_STATUS));
    assertThat(setPropertyElements.get(0).getAttributeValue(VALUE_ATTR_NAME), is(VALUE_403));
    assertThat(setPropertyElements.get(1).getAttributeValue(PROPERTY_NAME_ATTR_NAME), is(CONTENT_TYPE));
    assertThat(setPropertyElements.get(1).getAttributeValue(VALUE_ATTR_NAME), is(VALUE_APP_XML));
    assertThat(setPropertyElements.get(2).getAttributeValue(PROPERTY_NAME_ATTR_NAME), is(CONTENT_TYPE));
    assertThat(setPropertyElements.get(2).getAttributeValue(VALUE_ATTR_NAME), is(VALUE_APP_JSON));
    List<Element> setPayloadElements = element.getChildren(SET_PAYLOAD_TAG_NAME, MULE_4_POLICY_NAMESPACE);
    assertThat(setPayloadElements, notNullValue());
    assertThat(setPayloadElements.size(), is(2));
    assertThat(setPayloadElements.get(0).getAttributeValue(VALUE_ATTR_NAME), is(VALUE_SOAP_FAULT));
    assertThat(setPayloadElements.get(1).getAttributeValue(VALUE_ATTR_NAME), is(VALUE_FLOW_VARS));
  }

  private void assertProxyElement(Element element) {
    Element root = getRootElement(element);
    Element proxyElement = root.getChild(PROXY_TAG_NAME, HTTP_POLICY_NAMESPACE);
    assertThat(proxyElement, notNullValue());
    assertThat(proxyElement.getContentSize(), is(1));
    Element sourceElement = proxyElement.getChild(SOURCE_TAG_NAME, HTTP_POLICY_NAMESPACE);
    assertThat(sourceElement, notNullValue());
    assertThat(sourceElement.getContentSize(), is(1));
    Element tryElement = sourceElement.getChild(TRY_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
    assertThat(tryElement, notNullValue());
    assertThat(tryElement.getContentSize(), is(2));
    Element executeNextElement = tryElement.getChild(EXECUTE_NEXT_TAG_NAME, HTTP_POLICY_NAMESPACE);
    assertThat(executeNextElement, notNullValue());
    Element errorHandlerElement = tryElement.getChild(ERROR_HANDLER_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
    assertThat(errorHandlerElement, notNullValue());
    Element onErrorContinueElement = errorHandlerElement.getChild(ON_ERROR_CONTINUE_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
    assertThat(onErrorContinueElement, notNullValue());
    assertThat(onErrorContinueElement.getAttributeValue(TYPE_ATTR_NAME), is(IP_REJECTED));
    assertThat(onErrorContinueElement.getAttributeValue(LOG_EXCEPTION_ATTR_NAME), is(FALSE));
    assertThat(onErrorContinueElement.getContentSize(), is(9));
    assertProcessorChain(onErrorContinueElement);
  }

  @Test
  public void convertRawIpFilterTagWithNoProcessorChain() {
    final IpFilterTagMigrationStep step = new IpFilterTagMigrationStep();
    Element element = getTestElement();

    step.execute(element, reportMock);

    assertFilterTag(element);
  }

  @Test
  public void convertIpFilterTagWithContentAndNoProcessorChain() {
    final IpFilterTagMigrationStep step = new IpFilterTagMigrationStep();
    Element element = getTestElement()
        .addContent(new Element(GENERIC_TAG_NAME, IP_FILTER_GW_NAMESPACE));

    step.execute(element, reportMock);

    assertFilterTag(element);

    Element ipsElement = (Element) element.getContent(0);
    assertThat(ipsElement.getContentSize(), is(0));
  }

  @Test
  public void convertRawIpFilterTagWithProcessorChain() {
    final IpFilterTagMigrationStep step = new IpFilterTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement();
    addProcessorChainElement(element);

    step.execute(element, reportMock);

    assertFilterTag(element);
    assertDwlScript();
    assertProxyElement(element);
  }

  @Test
  public void convertIpFilterTagWithContentAndProcessorChain() {
    final IpFilterTagMigrationStep step = new IpFilterTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement()
        .addContent(new Element(GENERIC_TAG_NAME, IP_FILTER_GW_NAMESPACE));
    addProcessorChainElement(element);

    step.execute(element, reportMock);

    assertFilterTag(element);

    Element ipsElement = (Element) element.getContent(0);
    assertThat(ipsElement.getContentSize(), is(0));
    assertDwlScript();
    assertProxyElement(element);
  }
}
