package com.mulesoft.tools.migration.task.steps;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

public class DeleteNamespace extends MigrationStep {

    private String namespace;
    private String namespaceUri;
    private String schemaLocationUrl;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespaceUri() {
        return namespaceUri;
    }

    public void setNamespaceUri(String namespaceUri) {
        this.namespaceUri = namespaceUri;
    }

    public String getSchemaLocationUrl() {
        return schemaLocationUrl;
    }

    public void setSchemaLocationUrl(String schemaLocationUrl) {
        this.schemaLocationUrl = schemaLocationUrl;
    }

    public DeleteNamespace(String namespace, String namespaceUri, String schemaLocationUrl) {
        setNamespace(namespace);
        setNamespaceUri(namespaceUri);
        setSchemaLocationUrl(schemaLocationUrl);
    }

    public DeleteNamespace(){}

    public void execute() throws Exception {
        try {
            if(!StringUtils.isBlank(getNamespace()) && !StringUtils.isBlank(getNamespaceUri()) && !StringUtils.isBlank(getSchemaLocationUrl())) {
                Namespace nspc = Namespace.getNamespace(getNamespace(), getNamespaceUri());
                if (null != nspc && null != getDocument()) {
                    Element muleNode = getDocument().getRootElement();
                    muleNode.removeNamespaceDeclaration(nspc);
                    Attribute muleSchemaLocation = muleNode.getAttributes().get(0);
                    if (schemaLocationDefined(muleNode)) {
                        muleSchemaLocation.setValue(muleSchemaLocation.getValue().replace(getSchemaLocationUrl(), "").replace(getNamespaceUri(), ""));
                    }
                }
            }
        } catch (Exception ex) {
            throw new MigrationStepException("Delete node namespace exception. " + ex.getMessage());
        }
    }

    private boolean schemaLocationDefined(Element node) {
        Attribute att = node.getAttribute("schemaLocation", node.getNamespace("xsi"));
        if (att.getValue().contains(this.getNamespaceUri()) && att.getValue().contains(this.getSchemaLocationUrl())) {
            return true;
        } else {
            return false;
        }
    }
}
