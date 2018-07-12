/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.spring;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.util.version.VersionUtils.isVersionGreaterOrEquals;
import static java.lang.Boolean.parseBoolean;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrates the spring property placeholders.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SpringPropertiesPlaceholder extends AbstractSpringMigratorStep {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri()='http://www.springframework.org/schema/context' and local-name()='property-placeholder']";

  @Override
  public String getDescription() {
    return "Migrates the spring property placeholders.";
  }

  public SpringPropertiesPlaceholder() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    int idx = 1;
    for (String location : object.getAttributeValue("location").split("\\,")) {
      Element confProp = new Element("configuration-properties", CORE_NAMESPACE);
      confProp.setAttribute("file", location);

      if (object.getAttribute("file-encoding") != null) {
        if (isVersionGreaterOrEquals(getApplicationModel().getMuleVersion(), "4.2.0")) {
          confProp.setAttribute("encoding", object.getAttributeValue("file-encoding"));
        } else {
          report.report(ERROR, object, object,
                        "'file-encoding' is not available in Mule 4.1.x. It is included in 4.2.0 or higher.");
        }
      }

      object.getDocument().getRootElement().addContent(idx, confProp);
    }

    if (object.getAttribute("order") != null) {
      report.report(ERROR, object, object,
                    "'order' is no longer available. The properties are resolved in the order they were declared.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/configuring-properties#properties-files");
    }
    if (object.getAttributeValue("properties-ref") != null
        || parseBoolean(object.getAttributeValue("ignore-resource-not-found", "false"))
        || parseBoolean(object.getAttributeValue("ignore-unresolvable", "false"))
        || parseBoolean(object.getAttributeValue("local-override", "false"))
        || parseBoolean(object.getAttributeValue("system-properties-mode", "false"))) {
      report.report(ERROR, object, object,
                    "Spring specific attributes are no loger available. The default behavior cannot be changed in Mule 4.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/configuring-properties");
    }

    object.detach();
  }

}
