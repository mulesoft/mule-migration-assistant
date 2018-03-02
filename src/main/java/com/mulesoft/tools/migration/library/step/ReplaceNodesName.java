/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.step;

import com.mulesoft.tools.migration.engine.step.DefaultMigrationStep;
import com.mulesoft.tools.migration.engine.exception.MigrationStepException;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 *   Change node name
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ReplaceNodesName /*extends DefaultMigrationStep */ {

  private String nodeNamespace;
  private String newNodeName;

  public ReplaceNodesName() {}

  public ReplaceNodesName(String nodeNamespace, String newNodeName) {
    setNodeNamespace(nodeNamespace);
    setNewNodeName(newNodeName);
  }

  public void execute() throws Exception {
    try {
      //      if (getDocument() != null) {
      //        Namespace namespace = getDocument().getRootElement().getNamespace(getNodeNamespace());
      //        if (namespace != null) {
      //          for (Element node : getNodes()) {
      //            String legacyNode = node.getQualifiedName();
      //            node.setNamespace(namespace);
      //            node.setName(getNewNodeName());
      //            //            getReportingStrategy().log("Node <" + legacyNode + "> has been replaced with <" + node.getQualifiedName() + "> node",
      //            //                                       RULE_APPLIED, this.getDocument().getBaseURI(), null, this);
      //          }
      //        }
      //      }
    } catch (Exception ex) {
      throw new MigrationStepException("Replace node name exception. " + ex.getMessage());
    }
  }

  public void replaceChildNodesNamespace(Element node, Namespace namespace) {
    node.setNamespace(namespace);
    if (node.getChildren().size() > 0) {
      for (Element childNode : node.getChildren()) {
        replaceChildNodesNamespace(childNode, namespace);
      }
    }
  }

  public String getNodeNamespace() {
    return nodeNamespace;
  }

  public void setNodeNamespace(String nodeNamespace) {
    this.nodeNamespace = nodeNamespace;
  }

  public String getNewNodeName() {
    return newNodeName;
  }

  public void setNewNodeName(String newNodeName) {
    this.newNodeName = newNodeName;
  }

}
