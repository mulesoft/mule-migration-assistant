package com.mulesoft.tools.migration.task.steps;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.jdom2.Element;
import org.jdom2.Namespace;

public class MoveNodeToParentNode extends MigrationStep {

    private String sourceNode;
    private String sourceNodeNamespace;
    private String sourceNodeNamespaceUri;

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

    public MoveNodeToParentNode(String sourceNode, String sourceNodeNamespace, String sourceNodeNamespaceUri) {
        setSourceNode(sourceNode);
        setSourceNodeNamespace(sourceNodeNamespace);
        setSourceNodeNamespaceUri(sourceNodeNamespaceUri);
    }

    public MoveNodeToParentNode(){}

    public void execute() throws Exception {
        try {
            Namespace sourceNamespace = Namespace.getNamespace(getSourceNodeNamespace(), getSourceNodeNamespaceUri());
            for (Element node : getNodes()) {
                Element sourceElement = node.getChild(getSourceNode(),sourceNamespace);
                if (sourceElement != null) {
                    node.removeChild(getSourceNode(),sourceNamespace);
                    node.getParentElement().getChildren().add(sourceElement);
                }
            }
        } catch (Exception ex) {
            throw new MigrationStepException("Move node to parent exception. " + ex.getMessage());
        }
    }
}
