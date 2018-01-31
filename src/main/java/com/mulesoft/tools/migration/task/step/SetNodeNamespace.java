/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;

public class SetNodeNamespace extends MigrationStep {

    private String newNamespace;
    private String newNamespaceUri;
    private String schemaLocationUrl;

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
                        String legacyNode = node.getQualifiedName();
                        node.setNamespace(nspc);

                        getReportingStrategy().log("Node namespace has changed, node <" + legacyNode + "> changed to <" + node.getQualifiedName() + ">", RULE_APPLIED, this.getDocument().getBaseURI(), null , this);
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
}
