/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.soapkit.steps;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.CDATA;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.io.IOException;
import java.util.List;

import static com.mulesoft.tools.migration.library.soapkit.helpers.DataWeaveHelper.getMigrationScriptFolder;
import static com.mulesoft.tools.migration.library.soapkit.helpers.DataWeaveHelper.library;
import static com.mulesoft.tools.migration.library.soapkit.helpers.DocumentHelper.renameAttribute;
import static com.mulesoft.tools.migration.library.soapkit.helpers.DocumentHelper.replaceAttributeValue;
import static java.lang.System.lineSeparator;
import static java.util.Arrays.asList;

/**
 * Migrates the router configuration of APIkit for SOAP
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SoapkitRouterConfig extends AbstractSoapkitMigrationStep {

  private static final String XPATH_SELECTOR = "//*[local-name()='config' and namespace-uri()='" + SOAPKIT_NAMESPACE_URI + "']";

  private static final String EE_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/ee/core";
  private static final String EE_NAMESPACE_SCHEMA = "http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd";
  private static final String EE_NAMESPACE_NAME = "ee";
  private static final Namespace EE_NAMESPACE = Namespace.getNamespace(EE_NAMESPACE_NAME, EE_NAMESPACE_URI);

  private static final String PAYLOAD_MAPPING_SCRIPT = "%dw 2.0\n" +
      "output application/java\n" +
      "import migration::Soapkit\n" +
      "---\n" +
      "{\n" +
      "  body: Soapkit::soapBody(payload),\n" +
      "  headers: Soapkit::soapHeaders(vars),\n" +
      "  attachments: Soapkit::soapAttachments(vars)\n" +
      "}";

  @Override
  public String getDescription() {
    return "Update APIkit for SOAP config";
  }

  public SoapkitRouterConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(asList(SOAPKIT_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) {
    soapkitLib(getApplicationModel());

    final String originalConfigName = element.getAttributeValue("name");
    if (originalConfigName != null) {
      final String migratedConfigName = migrateConfigName(originalConfigName);
      migrateConfigNameInRouter(element, originalConfigName, migratedConfigName);
      migrateConfigNameInFlows(element, originalConfigName, migratedConfigName);
      replaceAttributeValue(element, "name", value -> migratedConfigName);
    }

    renameAttribute(element, "inboundValidationMessage", "inboundValidationEnabled");
    renameAttribute(element, "wsdlUrl", "wsdlLocation");
    renameAttribute(element, "serviceName", "service");
    renameAttribute(element, "portName", "port");
  }

  private void migrateConfigNameInFlows(Element element, String originalConfigName, String migratedConfigName) {
    getElementByXpath(element.getDocument(), getSoapkitFlowXPathSelector(originalConfigName))
        .forEach(flow -> {
          replaceConfigNameInAttribute(flow, "name", originalConfigName, "\\" + migratedConfigName);
          addPayloadMapping(flow);
        });
  }

  private void migrateConfigNameInRouter(Element element, String originalConfigName, String migratedConfigName) {
    getElementByXpath(element.getDocument(), getSoapkitRouterXPathSelector(originalConfigName))
        .forEach(router -> replaceConfigNameInAttribute(router, "config-ref", originalConfigName, migratedConfigName));
  }

  private void replaceConfigNameInAttribute(Element element, String attributeName, String originalConfigName,
                                            String migratedConfigName) {
    final String nameWithInvertedSlashes = originalConfigName.replaceAll("/", "\\\\");
    replaceAttributeValue(element, attributeName, value -> {
      if (value.contains(originalConfigName) || originalConfigName.equals(nameWithInvertedSlashes))
        return value.replace(originalConfigName, migratedConfigName);
      else
        return value.replace(nameWithInvertedSlashes, migratedConfigName);
    });
  }

  private String migrateConfigName(String value) {
    String result = value.trim();
    result = result.replaceAll("/", "-").replaceAll("\\\\", "-");
    return result.startsWith("-") ? result.substring(1) : result;
  }

  private String getSoapkitFlowXPathSelector(String originalConfigName) {
    final String nameWithInvertedSlashes = originalConfigName.replaceAll("/", "\\\\");
    return "//*[local-name()='flow' and ( ends-with(@name, '" + originalConfigName
        + "') or ends-with(@name, '" + nameWithInvertedSlashes + "') )]";
  }

  private String getSoapkitRouterXPathSelector(String originalConfigName) {
    final String nameWithInvertedSlashes = originalConfigName.replaceAll("/", "\\\\");
    return "//*[local-name()='router' and namespace-uri()='" + SOAPKIT_NAMESPACE_URI + "' and ( ends-with(@config-ref, '"
        + originalConfigName
        + "') or ends-with(@config-ref, '" + nameWithInvertedSlashes + "') )]";
  }

  private void addPayloadMapping(Element flow) {
    final Element transform = new Element("transform", EE_NAMESPACE);
    final Element message = new Element("message", EE_NAMESPACE);
    final Element setPayload = new Element("set-payload", EE_NAMESPACE);
    setPayload.addContent(new CDATA(PAYLOAD_MAPPING_SCRIPT));

    message.addContent(setPayload);
    transform.addContent(message);
    flow.addContent(transform);

    getApplicationModel().addNameSpace(EE_NAMESPACE_NAME, EE_NAMESPACE_URI, EE_NAMESPACE_SCHEMA);
  }

  private List<Element> getElementByXpath(Document document, String xpath) {
    XPathExpression<Element> compiledXPath = XPathFactory.instance().compile(xpath, Filters.element());
    return compiledXPath.evaluate(document);
  }

  public static void soapkitLib(ApplicationModel appModel) {
    try {
      library(getMigrationScriptFolder(appModel.getProjectBasePath()), "Soapkit.dwl",
              "" +
                  "/**" + lineSeparator() +
                  " * Write the body as xml string" + lineSeparator() +
                  " */" + lineSeparator() +
                  "fun soapBody(body: Any) = do { " + lineSeparator() +
                  "    if (typeOf(body) as String == \"String\") body " + lineSeparator() +
                  "    else body write \"application/xml\"" + lineSeparator() +
                  "}" + lineSeparator() +
                  lineSeparator() +
                  "/**" + lineSeparator() +
                  " * Get Soap headers from vars by filtering properties starting with 'soap.'" + lineSeparator() +
                  " */" + lineSeparator() +
                  "fun soapHeaders(vars: {}) = do {" + lineSeparator() +
                  "    var matcher_regex = /(?i)soap\\..*/" + lineSeparator() +
                  "    ---" + lineSeparator() +
                  "    vars default {} " + lineSeparator() +
                  "        filterObject($$ matches matcher_regex)" + lineSeparator() +
                  "        mapObject {" + lineSeparator() +
                  "            (($$ as String)[5 to -1]): $ write \"application/xml\"" + lineSeparator() +
                  "        }" + lineSeparator() +
                  "}" + lineSeparator() +
                  "" + lineSeparator() +
                  "/**" + lineSeparator() +
                  " * Get attachments from vars by filtering properties starting with 'att_'" + lineSeparator() +
                  " */" + lineSeparator() +
                  "fun soapAttachments(vars: {}) = do {" + lineSeparator() +
                  "    var matcher_regex = /(?i)att_.*/" + lineSeparator() +
                  "    ---" + lineSeparator() +
                  "    vars default {} " + lineSeparator() +
                  "        filterObject($$ matches matcher_regex)" + lineSeparator() +
                  "        mapObject {" + lineSeparator() +
                  "            (($$ as String)[4 to -1]): {" + lineSeparator() +
                  "                content: $," + lineSeparator() +
                  "                contentType: $.^mimeType" + lineSeparator() +
                  "            }" + lineSeparator() +
                  "        }" + lineSeparator() +
                  "}" + lineSeparator() +
                  lineSeparator());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
