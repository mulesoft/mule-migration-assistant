package com.mulesoft.tools.migration.library.mule.steps.salesforce;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;

public class CreateOperation extends AbstractApplicationModelMigrationStep {

    public static final String MULE3_SALESFORCE_NAMESPACE_PREFIX = "sfdc";
    public static final String MULE4_SALESFORCE_NAMESPACE_PREFIX = "salesforce";
    public static final String MULE3_SALESFORCE_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/sfdc";
    public static final String MULE4_SALESFORCE_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/salesforce";
    private static final Namespace MULE3_SALESFORCE_NAMESPACE = Namespace.getNamespace(MULE3_SALESFORCE_NAMESPACE_PREFIX, MULE3_SALESFORCE_NAMESPACE_URI);
    private static final Namespace MULE4_SALESFORCE_NAMESPACE = Namespace.getNamespace(MULE4_SALESFORCE_NAMESPACE_PREFIX, MULE4_SALESFORCE_NAMESPACE_URI);
    public static final String XPATH_SELECTOR = "/*/*[namespace-uri()='" + MULE3_SALESFORCE_NAMESPACE_URI + "' and local-name()='create']";

    public CreateOperation() {
        this.setAppliedTo(XPATH_SELECTOR);
        this.setNamespacesContributions(newArrayList(MULE3_SALESFORCE_NAMESPACE));
    }

    @Override
    public void execute(Element originalCreateOperation, MigrationReport report) throws RuntimeException {
        Element mule4CreateOperation = new Element("create", MULE4_SALESFORCE_NAMESPACE);
        setAttributes(originalCreateOperation, mule4CreateOperation);
        Optional<Element> mule3Headers =
                Optional.ofNullable(originalCreateOperation.getChild("headers", MULE3_SALESFORCE_NAMESPACE));

        mule3Headers.ifPresent(headers -> {
            originalCreateOperation.removeContent(headers);
            String refHeaders = headers.getAttributeValue("ref");
            if (refHeaders != null) {
                mule4CreateOperation.setAttribute("headers", refHeaders);
            }
            report.report("salesforce.create", originalCreateOperation, mule4CreateOperation);
        });

        Optional<Element> objects =
                Optional.ofNullable(originalCreateOperation.getChild("objects", MULE3_SALESFORCE_NAMESPACE));

        objects.ifPresent(records -> {
            originalCreateOperation.removeContent(records);
            Element recordsChild = new Element("records", MULE4_SALESFORCE_NAMESPACE);
            recordsChild.setAttribute("ref", originalCreateOperation.getAttributeValue("ref"));
            mule4CreateOperation.addContent(recordsChild);
        });

        originalCreateOperation.addContent(mule4CreateOperation);
        originalCreateOperation.setName("create");
        originalCreateOperation.setNamespace(MULE4_SALESFORCE_NAMESPACE);
    }

    private void setAttributes(Element mule3CreateOperation, Element mule4CreateOperation) {
        mule4CreateOperation.setAttribute("doc:name", mule3CreateOperation.getAttributeValue("doc:name"));
        mule4CreateOperation.setAttribute("config-ref", mule3CreateOperation.getAttributeValue("config-ref"));
        mule4CreateOperation.setAttribute("type", mule3CreateOperation.getAttributeValue("type"));
    }
}
