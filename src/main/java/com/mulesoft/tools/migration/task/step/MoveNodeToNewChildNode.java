/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.jdom2.Element;
import org.jdom2.Namespace;

import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;

public class MoveNodeToNewChildNode extends MigrationStep {

    private String sourceNode;
    private String sourceNodeNamespace;
    private String sourceNodeNamespaceUri;
    private String targetNode;
    private String targetNodeNamespace;
    private String targetNodeNamespaceUri;

    public MoveNodeToNewChildNode(String sourceNode, String sourceNodeNamespace, String sourceNodeNamespaceUri, String targetNode, String targetNodeNamespace, String targetNodeNamespaceUri) {
        setSourceNode(sourceNode);
        setSourceNodeNamespace(sourceNodeNamespace);
        setSourceNodeNamespaceUri(sourceNodeNamespaceUri);
        setTargetNode(targetNode);
        setTargetNodeNamespace(targetNodeNamespace);
        setTargetNodeNamespaceUri(targetNodeNamespaceUri);
    }

    public MoveNodeToNewChildNode(){}

    public void execute() throws Exception {
        try {
            Namespace sourceNamespace = Namespace.getNamespace(getSourceNodeNamespace(), getSourceNodeNamespaceUri());
            Namespace targetNamespace = Namespace.getNamespace(getTargetNodeNamespace(), getTargetNodeNamespaceUri());
            Element newTargetElement = null;
            for (Element node : getNodes()) {
                Element sourceElement = node.getChild(getSourceNode(),sourceNamespace);
                if (sourceElement != null) {
                    Element targetElement = node.getChild(getTargetNode(),targetNamespace);
                    if (targetElement != null) {
                        node.removeChild(getSourceNode(),sourceNamespace);
                        targetElement.getChildren().add(sourceElement);
                    } else {
                        newTargetElement = new Element(getTargetNode(),targetNamespace);
                        node.removeChild(getSourceNode(),sourceNamespace);
                        newTargetElement.getChildren().add(sourceElement);
                        node.addContent(newTargetElement);
                    }
                    getReportingStrategy().log("<" + sourceElement.getQualifiedName() + "> node is now a child of <" + (targetElement != null ? targetElement.getQualifiedName() : newTargetElement.getQualifiedName()) + "> node", RULE_APPLIED, this.getDocument().getBaseURI(), null , this);
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
