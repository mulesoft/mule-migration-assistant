package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.jdom2.Element;
import org.jdom2.Namespace;

import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;

public class MoveNodeToParentNode extends MigrationStep {

    private String sourceNode;
    private String sourceNodeNamespace;
    private String sourceNodeNamespaceUri;

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

                    getReportingStrategy().log("Node <" + sourceElement.getQualifiedName() + "> moved to parent node <" + node.getParentElement().getQualifiedName() + ">" , RULE_APPLIED, this.getDocument().getBaseURI(), null , this);
                }
            }
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
