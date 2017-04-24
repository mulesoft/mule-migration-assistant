package com.mulesoft.tools.migration.task.steps;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import static com.mulesoft.tools.migration.dom.DomUtils.findChildElement;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class MoveAttribute extends MigrationStep {

    private String attribute;
    private String targetNode;
    private String targetNodeNamespace;
    private String targetNodeNamespaceUri;
    private String sourceReferenceAttribute;
    private String targetReferenceAttribute;

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
                            }
                        }
                    }
                }
            }
        }catch (Exception ex) {
            throw new MigrationStepException("Move attribute exception. " + ex.getMessage());
        }
    }
}
