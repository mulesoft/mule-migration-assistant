package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import static com.mulesoft.tools.migration.tools.dom.DomUtils.findChildElement;
import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class GetAttributesFromReference extends MigrationStep {


    private String sourceReferenceAttribute;
    private String targetReferenceAttribute;
    private String referenceNode;
    private String referenceNodeNamespace;
    private String referenceNodeNamespaceUri;
    private String referenceChildNode;
    private String referenceAttributes;

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

    public GetAttributesFromReference(){}

    public void execute() throws Exception {
        try {

            if (!isBlank(getSourceReferenceAttribute()) && !isBlank(getTargetReferenceAttribute())) {

                String referenceValue;
                Namespace namespace;

                for (Element node : getNodes()) {
                    referenceValue = node.getAttributeValue(getSourceReferenceAttribute());
                    namespace = Namespace.getNamespace(getReferenceNodeNamespace(), getReferenceNodeNamespaceUri());
                    if(null != namespace) {
                        Element referenceElement = findChildElement(getReferenceNode(), referenceValue, getTargetReferenceAttribute(),
                                namespace, getDocument().getRootElement());
                        if (null != referenceElement && null != getReferenceChildNode()) {
                            Element referenceChildElement = findChildElement(getReferenceChildNode(), referenceElement);
                            if (null != referenceChildElement) {
                                String [] attributesArray = getReferenceAttributes().split(";");
                                for (String attribute  : attributesArray) {
                                    Attribute originalAttribute = referenceChildElement.getAttribute(attribute);
                                    if(null != originalAttribute) {
                                        Attribute newAttribute = new Attribute(originalAttribute.getName(), originalAttribute.getValue());
                                        node.setAttribute(newAttribute);

                                        getReportingStrategy().log("Get attribute from reference:" + newAttribute, RULE_APPLIED, this.getDocument().getBaseURI(), null , this);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }catch (Exception ex) {
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
