/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addCompatibilityNamespace;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateExpression;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Migrate MessagePropertiesTransformer to individual processors.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MessagePropertiesTransformer extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("message-properties-transformer");
  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update message-properties-transformer to individual processors.";
  }

  public MessagePropertiesTransformer() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(COMPATIBILITY_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    addCompatibilityNamespace(element.getDocument());
    if (element.getAttribute("scope") == null) {
      report.report("message.outboundProperties", element, element.getParentElement());
    }
    if ("session".equals(element.getAttributeValue("scope"))) {
      report.report("message.sessionVars", element, element.getParentElement());
    }

    boolean notOverwrite = false;
    if (element.getAttribute("overwrite") != null && "false".equals(element.getAttributeValue("overwrite"))) {
      notOverwrite = true;
    }

    int index = element.getParent().indexOf(element);

    List<Element> children = new ArrayList<>();

    for (Element child : element.getChildren()) {
      if ("delete-message-property".equals(child.getName())) {
        children.add(child);
        if (element.getAttribute("scope") == null) {
          child.setNamespace(COMPATIBILITY_NAMESPACE);
          child.setName("remove-property");
          child.getAttribute("key").setName("propertyName");
        } else if ("session".equals(element.getAttributeValue("scope"))) {
          child.setNamespace(COMPATIBILITY_NAMESPACE);
          child.setName("remove-session-variable");
          child.getAttribute("key").setName("variableName");
        } else {
          // invocation -> var
          child.setName("remove-variable");
          child.getAttribute("key").setName("variableName");
        }
      } else if ("add-message-property".equals(child.getName())) {
        children.add(child);
        if (element.getAttribute("scope") == null) {
          if (notOverwrite) {
            setMelExpressionValue(child, child.getAttributeValue("value"), "message.outboundProperties");
          }

          child.setNamespace(COMPATIBILITY_NAMESPACE);
          child.setName("set-property");
          child.getAttribute("key").setName("propertyName");
        } else if ("session".equals(element.getAttributeValue("scope"))) {
          if (notOverwrite) {
            setMelExpressionValue(child, child.getAttributeValue("value"), "sessionVars");
          }

          child.setNamespace(COMPATIBILITY_NAMESPACE);
          child.setName("set-session-variable");
          child.getAttribute("key").setName("variableName");
        } else {
          // invocation -> var
          if (notOverwrite) {
            String value = child.getAttributeValue("value");
            String migrated = getExpressionMigrator().migrateExpression(value, true, child);

            if (getExpressionMigrator().isWrapped(value) && migrated.startsWith("#[mel:")) {
              setMelExpressionValue(child, value, "vars");
            } else {
              child.getAttribute("value")
                  .setValue(getExpressionMigrator()
                      .wrap("vars['" + child.getAttributeValue("key") + "'] default "
                          + (getExpressionMigrator().isWrapped(migrated) ? "(" + getExpressionMigrator().unwrap(migrated) + ")"
                              : "'" + value + "'")));
            }
          } else {
            migrateExpression(child.getAttribute("value"), expressionMigrator);
          }

          child.setName("set-variable");
          child.getAttribute("key").setName("variableName");
        }
      } else if ("rename-message-property".equals(child.getName())) {
        // MessagePropertiesTransformer in 3.x doesn't use the 'overwrite' flag when renaming, so we do not contemplate that
        // case in the migrator
        if (element.getAttribute("scope") == null) {
          child.setNamespace(COMPATIBILITY_NAMESPACE);
          children.add(new Element("set-property", COMPATIBILITY_NAMESPACE)
              .setAttribute("propertyName", child.getAttributeValue("value"))
              .setAttribute("value", getExpressionMigrator()
                  .wrap("mel:message.outboundProperties['" + child.getAttributeValue("key") + "']")));
          children.add(new Element("remove-property", COMPATIBILITY_NAMESPACE).setAttribute("propertyName",
                                                                                            child
                                                                                                .getAttributeValue("key")));
        } else if ("session".equals(element.getAttributeValue("scope"))) {
          child.setNamespace(COMPATIBILITY_NAMESPACE);
          children.add(new Element("set-session-variable", COMPATIBILITY_NAMESPACE)
              .setAttribute("variableName", child.getAttributeValue("value"))
              .setAttribute("value", getExpressionMigrator()
                  .wrap("mel:sessionVars['" + child.getAttributeValue("key") + "']")));
          children.add(new Element("remove-session-variable", COMPATIBILITY_NAMESPACE).setAttribute("variableName",
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
        report.report("message.springBeanDefinitionInsideMuleObject", child, element.getParentElement());
      }
    }

    for (Element child : children) {
      element.removeContent(child);
    }

    element.getParent().addContent(index, children);
    element.detach();
  }

  private void setMelExpressionValue(Element child, String value, String binding) {
    child.getAttribute("value")
        .setValue(getExpressionMigrator()
            .wrap("mel:" + binding + "['" + child.getAttributeValue("key") + "'] != null "
                + "? " + binding + "['" + child.getAttributeValue("key") + "'] "
                + ": "
                + (getExpressionMigrator().isWrapped(value) ? getExpressionMigrator().unwrap(value)
                    : "'" + value + "'")));
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
