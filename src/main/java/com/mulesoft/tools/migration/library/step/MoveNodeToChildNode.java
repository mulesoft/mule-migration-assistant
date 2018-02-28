/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.step;

import com.mulesoft.tools.migration.engine.MigrationStep;
import com.mulesoft.tools.migration.engine.exception.MigrationStepException;
import org.jdom2.Element;
import org.jdom2.Namespace;

import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;

/**
 * Moves a node to a child node
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MoveNodeToChildNode extends MigrationStep {

  private String sourceNode;
  private String sourceNodeNamespace;
  private String sourceNodeNamespaceUri;
  private String targetNode;
  private String targetNodeNamespace;
  private String targetNodeNamespaceUri;

  public MoveNodeToChildNode(String sourceNode, String sourceNodeNamespace, String sourceNodeNamespaceUri, String targetNode,
                             String targetNodeNamespace, String targetNodeNamespaceUri) {
    setSourceNode(sourceNode);
    setSourceNodeNamespace(sourceNodeNamespace);
    setSourceNodeNamespaceUri(sourceNodeNamespaceUri);
    setTargetNode(targetNode);
    setTargetNodeNamespace(targetNodeNamespace);
    setTargetNodeNamespaceUri(targetNodeNamespaceUri);
  }

  public MoveNodeToChildNode() {}

  public void execute() throws Exception {
    try {
      Namespace sourceNamespace = Namespace.getNamespace(getSourceNodeNamespace(), getSourceNodeNamespaceUri());
      Namespace targetNamespace = Namespace.getNamespace(getTargetNodeNamespace(), getTargetNodeNamespaceUri());
      for (Element node : getNodes()) {
        Element sourceElement = node.getChild(getSourceNode(), sourceNamespace);
        if (sourceElement != null) {
          Element targetElement = node.getChild(getTargetNode(), targetNamespace);
          if (targetElement != null) {
            node.removeChild(getSourceNode(), sourceNamespace);
            targetElement.getChildren().add(sourceElement);

            getReportingStrategy().log(
                                       "<" + sourceElement.getQualifiedName() + "> node is now a child of <"
                                           + targetElement.getQualifiedName() + "> node",
                                       RULE_APPLIED, this.getDocument().getBaseURI(), null, this);
          }
        }
      }
    } catch (Exception ex) {
      throw new MigrationStepException("Move attribute exception. " + ex.getMessage());
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

  public String getTargetNode() {
    return targetNode;
  }

  public void setTargetNode(String targetNode) {
    this.targetNode = targetNode;
  }

  public String getTargetNodeNamespace() {
    return targetNodeNamespace;
  }

  public void setTargetNodeNamespace(String targetNodeNamespace) {
    this.targetNodeNamespace = targetNodeNamespace;
  }

  public String getTargetNodeNamespaceUri() {
    return targetNodeNamespaceUri;
  }

  public void setTargetNodeNamespaceUri(String targetNodeNamespaceUri) {
    this.targetNodeNamespaceUri = targetNodeNamespaceUri;
  }
}
