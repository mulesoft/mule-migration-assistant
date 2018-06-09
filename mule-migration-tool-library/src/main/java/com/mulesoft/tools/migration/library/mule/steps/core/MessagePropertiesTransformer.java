/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

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
public class MessagePropertiesTransformer extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  private static final String COMPATIBILITY_NAMESPACE = "http://www.mulesoft.org/schema/mule/compatibility";

  public static final String XPATH_SELECTOR = "//*[local-name()='message-properties-transformer']";
  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update message-properties-transformer to individual processors.";
  }

  public MessagePropertiesTransformer() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    Namespace compatibilityNamespace = Namespace.getNamespace("compatibility", COMPATIBILITY_NAMESPACE);

    if (element.getAttribute("scope") == null) {
      report.report(WARN, element, element,
                    "Instead of using properties in the flow, its values must be set explicitly in the operation/listener.",
                    "https://docs.mulesoft.com/mule-user-guide/v/4.1/intro-mule-message#outbound-properties");
      element.setNamespace(compatibilityNamespace);
    }
    if ("session".equals(element.getAttributeValue("scope"))) {
      report.report(WARN, element, element,
                    "Instead of using session variables in the flow, use variables.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/intro-mule-message#session-properties");
      element.setNamespace(compatibilityNamespace);
    }

    boolean notOverwrite = false;
    if (element.getAttribute("overwrite") != null && "false".equals(element.getAttributeValue("overwrite"))) {
      notOverwrite = true;
    }

    int index = element.getParent().indexOf(element);

    List<Element> children = new ArrayList<>();

    for (Element child : element.getChildren()) {
      if ("delete-message-property".equals(child.getName())) {
        if (element.getAttribute("scope") == null) {
          child.setNamespace(compatibilityNamespace);
          child.setName("remove-property");
          child.getAttribute("key").setName("propertyName");
        } else if ("session".equals(element.getAttributeValue("scope"))) {
          child.setNamespace(compatibilityNamespace);
          child.setName("remove-session-variable");
          child.getAttribute("key").setName("variableName");
        } else {
          // invocation -> var
          child.setName("remove-variable");
          child.getAttribute("key").setName("variableName");
        }
        children.add(child);
      } else if ("add-message-property".equals(child.getName())) {

        if (element.getAttribute("scope") == null) {
          if (notOverwrite) {
            String value = child.getAttributeValue("value");
            child.getAttribute("value")
                .setValue(getExpressionMigrator()
                    .wrap("mel:message.outboundProperties['" + child.getAttributeValue("key") + "'] != null "
                        + "? message.outboundProperties['" + child.getAttributeValue("key") + "'] "
                        + ": "
                        + (getExpressionMigrator().isWrapped(value) ? getExpressionMigrator().unwrap(value)
                            : "'" + value + "'")));
          }

          child.setNamespace(compatibilityNamespace);
          child.setName("set-property");
          child.getAttribute("key").setName("propertyName");
        } else if ("session".equals(element.getAttributeValue("scope"))) {
          if (notOverwrite) {
            String value = child.getAttributeValue("value");
            child.getAttribute("value")
                .setValue(getExpressionMigrator()
                    .wrap("mel:sessionVars['" + child.getAttributeValue("key") + "'] != null "
                        + "? sessionVars['" + child.getAttributeValue("key") + "'] "
                        + ": "
                        + (getExpressionMigrator().isWrapped(value) ? getExpressionMigrator().unwrap(value)
                            : "'" + value + "'")));
          }

          child.setNamespace(compatibilityNamespace);
          child.setName("set-session-variable");
          child.getAttribute("key").setName("variableName");
        } else {
          // invocation -> var
          if (notOverwrite) {
            String value = child.getAttributeValue("value");
            child.getAttribute("value")
                .setValue(getExpressionMigrator()
                    .wrap("vars['" + child.getAttributeValue("key") + "'] != null "
                        + "? vars['" + child.getAttributeValue("key") + "'] "
                        + ": "
                        + (getExpressionMigrator().isWrapped(value) ? getExpressionMigrator().unwrap(value)
                            : "'" + value + "'")));
          }

          child.setName("set-variable");
          child.getAttribute("key").setName("variableName");
        }
        children.add(child);
      } else if ("rename-message-property".equals(child.getName())) {
        // MessagePropertiesTransformer in 3.x doesn't use the 'overwrite' flag when renaming, so we do not contemplate that
        // case in the migrator
        if (element.getAttribute("scope") == null) {
          child.setNamespace(compatibilityNamespace);
          children.add(new Element("set-property", compatibilityNamespace)
              .setAttribute("propertyName", child.getAttributeValue("value"))
              .setAttribute("value", getExpressionMigrator()
                  .wrap("mel:message.outboundProperties['" + child.getAttributeValue("key") + "']")));
          children.add(new Element("remove-property", compatibilityNamespace).setAttribute("propertyName",
                                                                                           child
                                                                                               .getAttributeValue("key")));
        } else if ("session".equals(element.getAttributeValue("scope"))) {
          child.setNamespace(compatibilityNamespace);
          children.add(new Element("set-session-variable", compatibilityNamespace)
              .setAttribute("variableName", child.getAttributeValue("value"))
              .setAttribute("value", getExpressionMigrator()
                  .wrap("mel:sessionVars['" + child.getAttributeValue("key") + "']")));
          children.add(new Element("remove-session-variable", compatibilityNamespace).setAttribute("variableName",
                                                                                                   child
                                                                                                       .getAttributeValue("key")));
        } else {
          children.add(new Element("set-variable", CORE_NAMESPACE)
              .setAttribute("variableName", child.getAttributeValue("value"))
              .setAttribute("value", getExpressionMigrator()
                  .wrap("vars['" + child.getAttributeValue("key") + "']")));
          children.add(new Element("remove-variable", CORE_NAMESPACE).setAttribute("variableName",
                                                                                   child.getAttributeValue("key")));
        }
      } else if ("add-message-properties".equals(child.getName())) {
        // TODO Migrate to spring module
        report.report(ERROR, child, element,
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

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }
}
