/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.secureprops;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static java.lang.Boolean.parseBoolean;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the secure property placeholders.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SecurePropertiesPlaceholder extends AbstractApplicationModelMigrationStep {

  private static final String SECURE_NS_URI = "http://www.mulesoft.org/schema/mule/secure-properties";
  public static final Namespace SECURE_NAMESPACE = Namespace.getNamespace("secure-properties", SECURE_NS_URI);

  public static final String XPATH_SELECTOR =
      "/mule:mule/*[namespace-uri()='http://www.mulesoft.org/schema/mule/secure-property-placeholder' and local-name()='config']";

  @Override
  public String getDescription() {
    return "Migrates the secure property placeholders.";
  }

  public SecurePropertiesPlaceholder() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    int idx = object.getParent().indexOf(object);
    int j = 1;
    for (String location : object.getAttributeValue("location").split("\\,")) {
      Element confProp = new Element("config", SECURE_NAMESPACE);
      confProp.setAttribute("file", location);
      if (object.getAttribute("fileEncoding") != null) {
        confProp.setAttribute("encoding", object.getAttributeValue("fileEncoding"));
      }
      confProp.setAttribute("key", object.getAttributeValue("key"));
      confProp.setAttribute("name", object.getAttributeValue("name") + (j > 1 ? "_" + j : ""));

      Element encryptProp = new Element("encrypt", SECURE_NAMESPACE);
      encryptProp.setAttribute("algorithm", object.getAttributeValue("encryptionAlgorithm", "AES"));
      encryptProp.setAttribute("mode", object.getAttributeValue("encryptionMode", "CBC"));

      confProp.addContent(encryptProp);
      object.getDocument().getRootElement().addContent(idx, confProp);

      report.report(ERROR, confProp, confProp,
                    "Review usages of properties defined in the referenced file and add the 'secure::' prefix to those.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-secure-properties-placeholder",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/secure-configuration-properties#usage");

      ++j;
    }

    if (parseBoolean(object.getAttributeValue("ignoreResourceNotFound", "false"))) {
      report.report(ERROR, object, object,
                    "'ignoreResourceNotFound' is no longer available. The deployment will fail if a referenced file does not exist.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/configuring-properties#properties-files");
    }
    if (parseBoolean(object.getAttributeValue("ignoreUnresolvablePlaceholders", "false"))) {
      report.report(ERROR, object, object,
                    "'ignoreUnresolvablePlaceholders' is no longer available. The deployment will fail if a referenced property is not defined.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/configuring-properties");
    }
    if (!"FALLBACK".equals(object.getAttributeValue("systemPropertiesMode", "FALLBACK"))) {
      report.report(ERROR, object, object,
                    "'systemPropertiesMode' is no longer available. The default behavior cannot be changed in Mule 4.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/configuring-properties#properties-hierarchy");
    }

    object.detach();
  }

}
