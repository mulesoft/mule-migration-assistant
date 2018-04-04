/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.step;

import com.mulesoft.tools.migration.engine.exception.MigrationStepException;
import org.jdom2.Namespace;

/**
 * Removes a child node
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class DeleteChildNode /*extends AbstractMigrationStep */ {

  private String node;
  private String nodeNamespace;
  private String nodeNamespaceUri;

  public DeleteChildNode(String node, String nodeNamespace, String nodeNamespaceUri) {
    setNode(node);
    setNodeNamespace(nodeNamespace);
    setNodeNamespaceUri(nodeNamespaceUri);
  }

  public DeleteChildNode() {}

  public void execute() throws Exception {
    try {
      Namespace namespace = Namespace.getNamespace(getNodeNamespace(), getNodeNamespaceUri());
      //      for (Element node : getNodes()) {
      //        Element element = node.getChild(getNode(), namespace);
      //        if (element != null) {
      //          node.removeChild(getNode(), namespace);
      //
      //          //          getReportingStrategy().log("Child Node <" + node.getQualifiedName() + "> was deleted", RULE_APPLIED,
      //          //                                     this.getDocument().getBaseURI(), null, this);
      //        }
      //      }
    } catch (Exception ex) {
      throw new MigrationStepException("Remove child node exception. " + ex.getMessage());
    }
  }

  public String getNode() {
    return node;
  }

  public void setNode(String node) {
    this.node = node;
  }

  public String getNodeNamespace() {
    return nodeNamespace;
  }

  public void setNodeNamespace(String nodeNamespace) {
    this.nodeNamespace = nodeNamespace;
  }

  public String getNodeNamespaceUri() {
    return nodeNamespaceUri;
  }

  public void setNodeNamespaceUri(String nodeNamespaceUri) {
    this.nodeNamespaceUri = nodeNamespaceUri;
  }
}
