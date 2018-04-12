/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.step;

import com.mulesoft.tools.migration.exception.MigrationStepException;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * something with attrs //TODO improve doc
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class GetAttributesFromReference /*extends AbstractMigrationStep */ {


  private String sourceReferenceAttribute;
  private String targetReferenceAttribute;
  private String referenceNode;
  private String referenceNodeNamespace;
  private String referenceNodeNamespaceUri;
  private String referenceChildNode;
  private String referenceAttributes;

  public GetAttributesFromReference() {}

  public GetAttributesFromReference(String sourceReferenceAttribute, String targetReferenceAttribute,
                                    String referenceNode, String referenceNodeNamespace,
                                    String referenceNodeNamespaceUri, String referenceChildNode,
                                    String referenceAttributes) {

    this.sourceReferenceAttribute = sourceReferenceAttribute;
    this.targetReferenceAttribute = targetReferenceAttribute;
    this.referenceNode = referenceNode;
    this.referenceNodeNamespace = referenceNodeNamespace;
    this.referenceNodeNamespaceUri = referenceNodeNamespaceUri;
    this.referenceChildNode = referenceChildNode;
    this.referenceAttributes = referenceAttributes;
  }

  public void execute() throws Exception {
    try {

      if (!isBlank(getSourceReferenceAttribute()) && !isBlank(getTargetReferenceAttribute())) {
        //        for (Element node : getNodes()) {
        //          String referenceValue = node.getAttributeValue(getSourceReferenceAttribute());
        //          Namespace namespace = Namespace.getNamespace(getReferenceNodeNamespace(), getReferenceNodeNamespaceUri());
        //
        //          if (null != namespace) {
        //            Element referenceElement = findChildElement(getReferenceNode(), referenceValue, getTargetReferenceAttribute(),
        //                                                        namespace, getDocument().getRootElement());
        //            if (null != referenceElement && null != getReferenceChildNode()) {
        //              Element referenceChildElement = findChildElement(getReferenceChildNode(), referenceElement);
        //              if (null != referenceChildElement) {
        //                String[] attributesArray = getReferenceAttributes().split(";");
        //
        //                for (String attribute : attributesArray) {
        //                  Attribute originalAttribute = referenceChildElement.getAttribute(attribute);
        //                  if (null != originalAttribute) {
        //                    Attribute newAttribute = new Attribute(originalAttribute.getName(), originalAttribute.getValue());
        //                    node.setAttribute(newAttribute);
        //
        //                    //                    getReportingStrategy().log("Get attribute from reference:" + newAttribute, RULE_APPLIED,
        //                    //                                               this.getDocument().getBaseURI(), null, this);
        //                  }
        //                }
        //              }
        //            }
        //          }
        //        }
      }
    } catch (Exception ex) {
      throw new MigrationStepException("Get attribute from reference exception. " + ex.getMessage());
    }
  }

  public String getSourceReferenceAttribute() {
    return sourceReferenceAttribute;
  }

  public void setSourceReferenceAttribute(String sourceReferenceAttribute) {
    this.sourceReferenceAttribute = sourceReferenceAttribute;
  }

  public String getTargetReferenceAttribute() {
    return targetReferenceAttribute;
  }

  public void setTargetReferenceAttribute(String targetReferenceAttribute) {
    this.targetReferenceAttribute = targetReferenceAttribute;
  }

  public String getReferenceNode() {
    return referenceNode;
  }

  public void setReferenceNode(String referenceNode) {
    this.referenceNode = referenceNode;
  }

  public String getReferenceNodeNamespace() {
    return referenceNodeNamespace;
  }

  public void setReferenceNodeNamespace(String referenceNodeNamespace) {
    this.referenceNodeNamespace = referenceNodeNamespace;
  }

  public String getReferenceNodeNamespaceUri() {
    return referenceNodeNamespaceUri;
  }

  public void setReferenceNodeNamespaceUri(String referenceNodeNamespaceUri) {
    this.referenceNodeNamespaceUri = referenceNodeNamespaceUri;
  }

  public String getReferenceChildNode() {
    return referenceChildNode;
  }

  public void setReferenceChildNode(String referenceChildNode) {
    this.referenceChildNode = referenceChildNode;
  }

  public String getReferenceAttributes() {
    return referenceAttributes;
  }

  public void setReferenceAttributes(String referenceAttributes) {
    this.referenceAttributes = referenceAttributes;
  }
}
