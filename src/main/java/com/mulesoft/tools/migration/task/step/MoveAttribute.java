package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import static com.mulesoft.tools.migration.dom.DomUtils.findChildElement;
import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class MoveAttribute extends MigrationStep {

    private String attribute;
    private String targetNode;
    private String targetNodeNamespace;
    private String targetNodeNamespaceUri;
    private String sourceReferenceAttribute;
    private String targetReferenceAttribute;

    public MoveAttribute(String attribute, String targetNode, String targetNodeNamespace, String targetNodeNamespaceUri,
                         String sourceReferenceAttribute, String targetReferenceAttribute) {
        setAttribute(attribute);
        setTargetNode(targetNode);
        setTargetNodeNamespace(targetNodeNamespace);
        setTargetNodeNamespaceUri(targetNodeNamespaceUri);
        setSourceReferenceAttribute(sourceReferenceAttribute);
        setTargetReferenceAttribute(targetReferenceAttribute);
    }

    public MoveAttribute(){}

    public void execute() throws Exception {
        try {

            if (!isBlank(getAttribute()) && !isBlank(getTargetNode())
                    && !isBlank(getTargetNodeNamespace())
                    && !isBlank(getTargetNodeNamespaceUri())) {

                Attribute attribute;
                String referenceValue;

                for (Element node : getNodes()) {
                    attribute = node.getAttribute(getAttribute());
                    referenceValue = node.getAttributeValue(getSourceReferenceAttribute());
                    if (attribute != null) {
                        Namespace namespace = Namespace.getNamespace(getTargetNodeNamespace(), getTargetNodeNamespaceUri());
                        if(null != namespace) {
                            Element element = findChildElement(getTargetNode(), referenceValue, getTargetReferenceAttribute(), namespace, getDocument().getRootElement());
                            if (null != element) {
                                node.removeAttribute(attribute);
                                element.setAttribute(attribute);

                                getReportingStrategy().log("Moved " + attribute + " attribute into <" + node.getQualifiedName() + ">", RULE_APPLIED);
                            }
                        }
                    }
                }
            }
        }catch (Exception ex) {
            throw new MigrationStepException("Move attribute exception. " + ex.getMessage());
        }
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
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
}
