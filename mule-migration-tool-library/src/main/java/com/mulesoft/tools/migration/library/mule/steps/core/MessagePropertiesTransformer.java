/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;

import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.ArrayList;
import java.util.List;

/**
 * Migrate MessagePropertiesTransformer to individual processors.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MessagePropertiesTransformer extends AbstractApplicationModelMigrationStep {

  private static final String COMPATIBILITY_NAMESPACE = "http://www.mulesoft.org/schema/mule/compatibility";

  public static final String XPATH_SELECTOR = "//*[local-name()='message-properties-transformer']";

  @Override
  public String getDescription() {
    return "Update message-properties-transformer to individual processors.";
  }

  public MessagePropertiesTransformer() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {

    report.report(WARN, element, element,
                  "Instead of setting properties in the flow, its values must be set explicitly in the operation/listener.",
                  "https://docs.mulesoft.com/mule-user-guide/v/4.1/intro-mule-message#outbound-properties");
    Namespace compatibilityNamespace = Namespace.getNamespace("compatibility", COMPATIBILITY_NAMESPACE);
    element.setNamespace(compatibilityNamespace);

    boolean notOverwrite = false;
    if (element.getAttribute("overwrite") != null && "false".equals(element.getAttributeValue("overwrite"))) {
      notOverwrite = true;
    }

    int index = element.getParent().indexOf(element);

    List<Element> children = new ArrayList<>();

    for (Element child : element.getChildren()) {
      child.setNamespace(compatibilityNamespace);

      if ("delete-message-property".equals(child.getName())) {
        child.setName("remove-property");
        child.getAttribute("key").setName("propertyName");
        children.add(child);
      } else if ("add-message-property".equals(child.getName())) {

        if (notOverwrite) {
          String value = child.getAttributeValue("value");
          child.getAttribute("value")
              .setValue(getExpressionMigrator()
                  .wrap("mel:message.outboundProperties['" + child.getAttributeValue("key") + "'] != null "
                      + "? message.outboundProperties['" + child.getAttributeValue("key") + "'] "
                      + ": "
                      + (getExpressionMigrator().isWrapped(value) ? getExpressionMigrator().unwrap(value) : "'" + value + "'")));
        }

        child.setName("set-property");
        child.getAttribute("key").setName("propertyName");
        children.add(child);
      } else if ("rename-message-property".equals(child.getName())) {
        // MessagePropertiesTransformer in 3.x doesn't use the 'overwrite' flag when renaming, so we do not contemplate that case
        // in the migrator
        children.add(new Element("set-property", compatibilityNamespace)
            .setAttribute("propertyName", child.getAttributeValue("value"))
            .setAttribute("value", getExpressionMigrator()
                .wrap("mel:message.outboundProperties['" + child.getAttributeValue("key") + "']")));
        children.add(new Element("remove-property", compatibilityNamespace).setAttribute("propertyName",
                                                                                         child.getAttributeValue("key")));
      } else if ("add-message-properties".equals(child.getName())) {
        // TODO Migrate to spring module
        report
            .report(ERROR, child, element,
                    "Spring beans definition inside mule components is not allowed. This inner definition must be moved to its own spring config file, and it may be referenced by an `ee:transform` component or in an expression in the operation/listeenr directly.",
                    "https://docs.mulesoft.com/mule-user-guide/v/4.1/migration-module-spring");
      }
    }

    for (Element child : children) {
      element.removeContent(child);
    }

    element.getParent().addContent(index, children);
    element.getParent().removeContent(element);
  }

}
