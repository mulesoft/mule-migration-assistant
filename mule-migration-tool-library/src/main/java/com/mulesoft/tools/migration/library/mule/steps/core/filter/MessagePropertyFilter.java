/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.filter;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate MessageProperty filter to a validation stub
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MessagePropertyFilter extends AbstractFilterMigrator {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("message-property-filter");

  @Override
  public String getDescription() {
    return "Update MessageProperty filter to a validation stub.";
  }

  public MessagePropertyFilter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    addValidationsModule(element.getDocument());

    String pattern = element.getAttributeValue("pattern");
    String scope = element.getAttributeValue("scope");
    String propertyName;
    String propertyValue;
    boolean negated = false;

    int x = pattern.indexOf(":");
    int i = pattern.indexOf('=');

    if (x > -1 && x < i) {
      scope = pattern.substring(0, x);
      pattern = pattern.substring(x + 1);
      i = pattern.indexOf('=');
    }

    if (pattern.charAt(i - 1) == '!') {
      negated = true;
      propertyName = pattern.substring(0, i - 1).trim();
    } else {
      propertyName = pattern.substring(0, i).trim();
    }
    propertyValue = pattern.substring(i + 1).trim();

    if (propertyValue.contains("*")) {
      if (negated) {
        element.setAttribute("regex", "(?!^" + propertyValue.replaceAll("\\*", ".*") + "$)");
      } else {
        element.setAttribute("regex", "^" + propertyValue.replaceAll("\\*", ".*") + "$");
      }
      element.removeAttribute("pattern");
      element.removeAttribute("scope");
      element.setAttribute("value",
                           "#[vars.compatibility_" + (scope == null ? "outbound" : scope) + "Properties['" + propertyName
                               + "']]");
      element.setName("matches-regex");
      element.setNamespace(VALIDATION_NAMESPACE);
    } else {
      element.setAttribute("expression",
                           "#[vars.compatibility_" + (scope == null ? "outbound" : scope) + "Properties['" + propertyName + "'] "
                               + (negated ? "!=" : "==") + " '" + propertyValue + "']");
      element.removeAttribute("scope");
      element.removeAttribute("pattern");
      element.setName("is-true");
      element.setNamespace(VALIDATION_NAMESPACE);
    }

    handleFilter(element);
  }

}
