package com.mulesoft.tools.migration.task.steps;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

public class SetNodeNamespace extends MigrationStep {

    private String newNamespace;
    private String newNamespaceUri;
    private String schemaLocationUrl;

    public String getNewNamespace() {
        return newNamespace;
    }

    public void setNewNamespace(String newNamespace) {
        this.newNamespace = newNamespace;
    }

    public String getNewNamespaceUri() {
        return newNamespaceUri;
    }

    public void setNewNamespaceUri(String newNameSpaceUri) {
        this.newNamespaceUri = newNameSpaceUri;
    }

    public String getSchemaLocationUrl() {
        return schemaLocationUrl;
    }

    public void setSchemaLocationUrl(String schemaLocationUrl) {
        this.schemaLocationUrl = schemaLocationUrl;
    }

    public SetNodeNamespace(String newNamespace, String newNamespaceUri, String schemaLocationUrl) {
        setNewNamespace(newNamespace);
        setNewNamespaceUri(newNamespaceUri);
        setSchemaLocationUrl(schemaLocationUrl);
    }

    public SetNodeNamespace(){}

    public void execute() throws Exception {
        try {
            Namespace nspc = Namespace.getNamespace(getNewNamespace(), getNewNamespaceUri());
            if (nspc != null && getDocument() != null) {
                Element muleNode = getDocument().getRootElement();
                muleNode.addNamespaceDeclaration(nspc);
                Attribute muleSchemaLocation = muleNode.getAttributes().get(0);
                if (schemaLocationNotDefined(muleNode)) {
                    muleSchemaLocation.setValue(muleSchemaLocation.getValue() + " " + getNewNamespaceUri()
                                                    + " " + getSchemaLocationUrl() + " ");
                }
                if (this.getNodes() != null) {
                    for (Element node : this.getNodes()) {
                        node.setNamespace(nspc);
                    }
                }
            }
        } catch (Exception ex) {
            throw new MigrationStepException("Set node namespace exception. " + ex.getMessage());
        }
    }

    private boolean schemaLocationNotDefined(Element node) {
        Attribute att = node.getAttribute("schemaLocation", node.getNamespace("xsi"));
        if (att.getValue().contains(this.getNewNamespaceUri()) && att.getValue().contains(this.getSchemaLocationUrl())) {
            return false;
        } else {
            return true;
        }
    }
}
