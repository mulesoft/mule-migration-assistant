/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_POLICY_NAMESPACE;

import com.mulesoft.tools.migration.library.gateway.steps.GatewayMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Common stuff to migrate policy elements
 *
 * @author Mulesoft Inc.
 */
public abstract class PolicyMigrationStep extends GatewayMigrationStep {

  protected static final String MULE_4_TAG_NAME = "mule";
  protected static final String POLICY_NAME = "policyName";
  protected static final String ID = "id";

  private static final String PROXY_TAG_NAME = "proxy";
  private static final String SOURCE_TAG_NAME = "source";
  protected static final String EXECUTE_NEXT_TAG_NAME = "execute-next";
  protected static final String TRY_TAG_NAME = "try";
  protected static final String ERROR_HANDLER_TAG_NAME = "error-handler";
  protected static final String CONFIG_REF_ATTR_NAME = "config-ref";

  protected static final String VALUE_ATTR_NAME = "value";

  private static final String HTTP_POLICY_XSI_URI =
      "http://www.mulesoft.org/schema/mule/http-policy http://www.mulesoft.org/schema/mule/http-policy/current/mule-http-policy.xsd";

  public PolicyMigrationStep(final Namespace namespace, final String tagName) {
    super(namespace, tagName);
  }

  public PolicyMigrationStep() {

  }

  private String getPolicyName(Element element, Element proxyElement, MigrationReport migrationReport) {
    final String policyName = getRootElement(element).getAttributeValue(POLICY_NAME);
    if (policyName == null) {
      String defaultPolicyName = getApplicationModel().getPomModel().get().getArtifactId();
      migrationReport.report("basicStructure.defaultPolicyName", proxyElement, proxyElement, defaultPolicyName);
      return defaultPolicyName;
    }
    return policyName;
  }

  private void completeMule4Element(Element element) {
    Element root = getRootElement(element);
    root.addNamespaceDeclaration(HTTP_POLICY_NAMESPACE);
    Attribute attr = getXSIAttribute(root);
    final String uri = attr.getValue();
    attr.setValue(new StringBuilder(uri).append(SPACE).append(HTTP_POLICY_XSI_URI).toString());
  }

  private Element addHttpPolicyTag(Element element, boolean modifyCurrentElement, MigrationReport migrationReport) {
    completeMule4Element(element);
    Element sourceElement = new Element(SOURCE_TAG_NAME, HTTP_POLICY_NAMESPACE);
    sourceElement.addContent(new Element(EXECUTE_NEXT_TAG_NAME, HTTP_POLICY_NAMESPACE));
    if (modifyCurrentElement) {
      element.setName(PROXY_TAG_NAME);
      element.setNamespace(HTTP_POLICY_NAMESPACE);
      element.addContent(sourceElement);
      element.setAttribute(NAME_ATTR_NAME, getPolicyName(element, element, migrationReport));
      return element;
    }
    Element proxyElement = new Element(PROXY_TAG_NAME, HTTP_POLICY_NAMESPACE);
    proxyElement.addContent(sourceElement);
    proxyElement.setAttribute(NAME_ATTR_NAME, getPolicyName(element, proxyElement, migrationReport));
    getRootElement(element).addContent(proxyElement.detach());
    return proxyElement;
  }

  private Element getHttpPolicyFromDocument(final Element element) {
    return element.getChild(PROXY_TAG_NAME, HTTP_POLICY_NAMESPACE);
  }

  private boolean hasHttpPolicyBeenSetUp(final Document document) {
    return document != null && getHttpPolicyFromDocument(document.getRootElement()) != null;
  }

  protected Element setUpHttpPolicy(Element element, boolean modifyCurrentElement, MigrationReport migrationReport) {
    Element source;
    if (!hasHttpPolicyBeenSetUp(element.getDocument())) {
      source = addHttpPolicyTag(element, modifyCurrentElement, migrationReport).getChild(SOURCE_TAG_NAME, HTTP_POLICY_NAMESPACE);
    } else {
      Element proxyElement = getRootElement(element).getChild(PROXY_TAG_NAME, HTTP_POLICY_NAMESPACE);
      if (proxyElement.getAttributeValue(NAME_ATTR_NAME).equals(EMPTY)) {
        proxyElement.setAttribute(NAME_ATTR_NAME, getPolicyName(proxyElement, proxyElement, migrationReport));
      }
      source = proxyElement.getChild(SOURCE_TAG_NAME, HTTP_POLICY_NAMESPACE);
      if (modifyCurrentElement) {
        element.detach();
      }
    }
    return source;
  }

  protected int getElementPosition(Element parentElement, String tagName) {
    int[] count = new int[] {1};
    parentElement.getContent().stream().filter(content -> {
      if (content instanceof Element && ((Element) content).getName().equals(tagName)) {
        return true;
      }
      count[0]++;
      return false;
    }).findFirst();
    return count[0];
  }

}
