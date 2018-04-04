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
 * Move node to parent node
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MoveNodeToParentNode /*extends AbstractMigrationStep */ {

  private String sourceNode;
  private String sourceNodeNamespace;
  private String sourceNodeNamespaceUri;

  public MoveNodeToParentNode(String sourceNode, String sourceNodeNamespace, String sourceNodeNamespaceUri) {
    setSourceNode(sourceNode);
    setSourceNodeNamespace(sourceNodeNamespace);
    setSourceNodeNamespaceUri(sourceNodeNamespaceUri);
  }

  public MoveNodeToParentNode() {}

  public void execute() throws Exception {
    try {
      Namespace sourceNamespace = Namespace.getNamespace(getSourceNodeNamespace(), getSourceNodeNamespaceUri());
      //      for (Element node : getNodes()) {
      //        Element sourceElement = node.getChild(getSourceNode(), sourceNamespace);
      //        if (sourceElement != null) {
      //          node.removeChild(getSourceNode(), sourceNamespace);
      //          node.getParentElement().getChildren().add(sourceElement);
      //
      //          //          getReportingStrategy().log(
      //          //                                     "Node <" + sourceElement.getQualifiedName() + "> moved to parent node <"
      //          //                                         + node.getParentElement().getQualifiedName() + ">",
      //          //                                     RULE_APPLIED, this.getDocument().getBaseURI(), null, this);
      //        }
      //      }
    } catch (Exception ex) {
      throw new MigrationStepException("Move node to parent exception. " + ex.getMessage());
    }
  }

  public String getSourceNode() {
    return sourceNode;
  }

  public void setSourceNode(String sourceNode) {
    this.sourceNode = sourceNode;
  }

  public String getSourceNodeNamespace() {
    return sourceNodeNamespace;
  }

  public void setSourceNodeNamespace(String sourceNodeNamespace) {
    this.sourceNodeNamespace = sourceNodeNamespace;
  }

  public String getSourceNodeNamespaceUri() {
    return sourceNodeNamespaceUri;
  }

  public void setSourceNodeNamespaceUri(String sourceNodeNamespaceUri) {
    this.sourceNodeNamespaceUri = sourceNodeNamespaceUri;
  }
}
