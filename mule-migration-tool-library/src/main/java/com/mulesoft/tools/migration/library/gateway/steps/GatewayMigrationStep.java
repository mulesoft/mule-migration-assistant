/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_CORE_NAMESPACE_NO_PREFIX;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_POLICY_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.XSI_NAMESPACE;
import static java.util.Arrays.asList;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Gateway migration step
 *
 * @author Mulesoft Inc.
 */
public abstract class GatewayMigrationStep extends AbstractApplicationModelMigrationStep {

  protected static final String SPACE = " ";
  protected static final String NAME_ATTR_NAME = "name";
  protected static final String EMPTY = "";

  private static final String MULE_4_TAG_NAME = "mule";

  private static final String SCHEMA_LOCATION = "schemaLocation";

  private static final String MULE_4_XSI_SCHEMA_LOCATION_URI =
      "http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd";

  public GatewayMigrationStep(final Namespace namespace, final String tagName) {
    this.setNamespacesContributions(asList(namespace));
    this.setAppliedTo(getXPathSelector(namespace, tagName));
  }

  public GatewayMigrationStep() {

  }

  protected void removeAttributes(List<String> attributesToRemove, Element element) {
    attributesToRemove.forEach(attr -> element.removeAttribute(attr));
  }

  protected final String getXPathSelector(Namespace namespace, String tagName) {
    return "//*[namespace-uri() = '" + namespace.getURI() + "' and local-name() = '" + tagName + "']";
  }

  protected final String getXPathSelector(Namespace namespace, String tagName, String attributeName, String attributeValue) {
    return "//*[namespace-uri() = '" + namespace.getURI() + "' and local-name() = '" + tagName + "'][@*[local-name()='"
        + attributeName + "' and .='" + attributeValue + "']]";
  }

  protected final String getXPathSelector(String attributeValue) {
    return "//*[@*[contains(.,'" + attributeValue + "')]]";
  }

  protected final List<Content> detachContent(final List<Content> contentList) {
    final List<Content> result = new ArrayList<>();
    final int contentListSize = contentList.size();
    for (int i = 0; i < contentListSize; i++) {
      Content c = contentList.get(0);
      result.add(c.detach());
    }
    return result;
  }

  protected void addSchemaLocationNamespace(Element root, String mule4Uri) {
    Attribute attr = getXSIAttribute(root);
    attr.setValue(new StringBuilder(attr.getValue()).append(SPACE).append(mule4Uri).toString());
  }

  protected void removeSchemaLocationNamespace(Element root, String mule3Uri, String mule4Uri) {
    Attribute attr = getXSIAttribute(root);
    String uri = attr.getValue();
    if (uri.contains(mule4Uri)) {
      attr.setValue(uri.replace(mule3Uri, EMPTY));
    } else {
      attr.setValue(uri.replace(mule3Uri, mule4Uri));
    }
  }

  private void setUriValue(Attribute attr, final String uri, final String mule3Uri, final String mule4Uri) {
    if (uri.contains(mule3Uri)) {
      attr.setValue(uri.replace(mule3Uri, mule4Uri));
    } else if (!uri.contains(mule4Uri)) {
      attr.setValue(new StringBuilder(uri).append(SPACE).append(mule4Uri).toString());
    }
  }

  protected Attribute getXSIAttribute(Element element) {
    if (element == null) {
      return new Attribute(SCHEMA_LOCATION, MULE_4_XSI_SCHEMA_LOCATION_URI, XSI_NAMESPACE);
    }
    if (element.getAttribute(SCHEMA_LOCATION, XSI_NAMESPACE) == null) {
      Attribute attr = new Attribute(SCHEMA_LOCATION, MULE_4_XSI_SCHEMA_LOCATION_URI, XSI_NAMESPACE);
      element.setAttribute(attr);
      return attr;
    }
    return element.getAttribute(SCHEMA_LOCATION, XSI_NAMESPACE);
  }

  protected void replaceSchemaLocationNamespace(Element root, String mule3UriXsd, String mule4UriXsd, String mule3Uri,
                                                String mule4Uri) {
    Attribute attr = getXSIAttribute(root);
    setUriValue(attr, attr.getValue(), mule3UriXsd, mule4UriXsd);
    setUriValue(attr, attr.getValue(), mule3Uri, mule4Uri);
  }

  protected Element getRootElement(final Element element) {
    Document doc = element.getDocument();
    if (doc != null) {
      return doc.getRootElement();
    }
    Element e = new Element(MULE_4_TAG_NAME, MULE_4_POLICY_NAMESPACE).addContent(element);
    e.addNamespaceDeclaration(MULE_4_CORE_NAMESPACE_NO_PREFIX);
    new Document().setRootElement(e);
    return e;
  }

  protected void migrateRootElement(Element element, Namespace mule3Namespace, Namespace mule4Namespace, String mule3UriXsd,
                                    String mule4UriXsd, String mule3Uri,
                                    String mule4Uri) {
    Element root = getRootElement(element);
    if (root != null) {
      root.removeNamespaceDeclaration(mule3Namespace);
      root.addNamespaceDeclaration(mule4Namespace);
      replaceSchemaLocationNamespace(root, mule3UriXsd, mule4UriXsd, mule3Uri, mule4Uri);
    }
  }

  protected void addNamespaceDeclaration(Element root, Namespace namespace, String schemaLocationUri) {
    if (root != null) {
      root.addNamespaceDeclaration(namespace);
      addSchemaLocationNamespace(root, schemaLocationUri);
    }
  }

}
