/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.step;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import com.mulesoft.tools.migration.engine.exception.MigrationStepException;

/**
 * Removes a namespacePrefix from a file
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class DeleteNamespace /*extends AbstractMigrationStep */ {

  private String namespacePrefix;
  private String namespaceUri;
  private String schemaLocationUrl;

  public DeleteNamespace() {}

  public DeleteNamespace(String namespacePrefix, String namespaceUri, String schemaLocationUrl) {
    setNamespacePrefix(namespacePrefix);
    setNamespaceUri(namespaceUri);
    // setSchemaLocationUrl(schemaLocationUrl);
  }

  public void execute() throws Exception {
    try {
      if (!StringUtils.isBlank(getNamespacePrefix()) && !StringUtils.isBlank(getNamespaceUri())
          && !StringUtils.isBlank(getSchemaLocationUrl())) {
        Namespace namespaceToRemove = Namespace.getNamespace(getNamespacePrefix(), getNamespaceUri());
        //        Document document = getDocument();
        //        if (null != namespaceToRemove && null != document) {
        //
        //
        //          Element rootElement = document.getRootElement();
        //          rootElement.removeNamespaceDeclaration(namespaceToRemove);
        //
        //          Attribute schemaLocationAttribute = rootElement.getAttribute("schemaLocation", rootElement.getNamespace("xsi"));
        //          boolean b = schemaLocationAttribute.getValue().contains(this.getNamespaceUri()) &&
        //              schemaLocationAttribute.getValue().contains(this.getSchemaLocationUrl());
        //
        //          if (schemaLocationDefined(rootElement)) {
        //            String value = schemaLocationAttribute.getValue();
        //            value.replace(getSchemaLocationUrl(), EMPTY);
        //            value.replace(getNamespaceUri(), EMPTY);
        //
        //            schemaLocationAttribute.setValue(value);
        //
        //            // getReportingStrategy().log("Namespace " + namespacePrefix + ":" + nspc.getURI() + " was deleted", RULE_APPLIED,
        //            // this.getDocument().getBaseURI(), null, this);
        //          }
        //        }
      }
    } catch (Exception ex) {
      throw new MigrationStepException("Delete node namespacePrefix exception. " + ex.getMessage());
    }
  }

  private boolean schemaLocationDefined(Element node) {
    Attribute att = node.getAttribute("schemaLocation", node.getNamespace("xsi"));
    return att.getValue().contains(this.getNamespaceUri()) && att.getValue().contains(this.getSchemaLocationUrl());
  }

  public String getNamespacePrefix() {
    return namespacePrefix;
  }

  public void setNamespacePrefix(String namespacePrefix) {
    this.namespacePrefix = namespacePrefix;
  }

  public String getNamespaceUri() {
    return namespaceUri;
  }

  public void setNamespaceUri(String namespaceUri) {
    this.namespaceUri = namespaceUri;
  }

  public String getSchemaLocationUrl() {
    return schemaLocationUrl;
  }

  // public void setSchemaLocationUrl(String schemaLocationUrl) {
  // this.schemaLocationUrl = schemaLocationUrl;
  // }
}
