/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.security.properties;

import static com.mulesoft.tools.migration.util.version.VersionUtils.isVersionGreaterOrEquals;
import static java.lang.Boolean.parseBoolean;
import static java.util.Collections.singletonList;
import static org.jdom2.Namespace.getNamespace;

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
  private static final String SECURE_SCHEMA =
      "http://www.mulesoft.org/schema/mule/secure-properties/current/mule-secure-properties.xsd";
  public static final Namespace SECURE_NAMESPACE = Namespace.getNamespace("secure-properties", SECURE_NS_URI);

  public static final String XPATH_SELECTOR =
      "/*/*[namespace-uri()='http://www.mulesoft.org/schema/mule/secure-property-placeholder' and local-name()='config']";

  @Override
  public String getDescription() {
    return "Migrates the secure property placeholders.";
  }

  public SecurePropertiesPlaceholder() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(singletonList(getNamespace("secure-property-placeholder",
                                                               "http://www.mulesoft.org/schema/mule/secure-property-placeholder")));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    getApplicationModel().addNameSpace(SECURE_NAMESPACE, SECURE_SCHEMA, object.getDocument());
    int idx = object.getParent().indexOf(object);
    int j = 1;
    for (String location : object.getAttributeValue("location").split("\\,")) {
      Element confProp = new Element("config", SECURE_NAMESPACE);
      confProp.setAttribute("file", location);

      if (object.getAttribute("fileEncoding") != null) {
        if (isVersionGreaterOrEquals(getApplicationModel().getMuleVersion(), "4.2.0")) {
          confProp.setAttribute("encoding", object.getAttributeValue("fileEncoding"));
        } else {
          report.report("configProperties.encoding", object, object);
        }
      }

      confProp.setAttribute("key", object.getAttributeValue("key"));
      confProp.setAttribute("name", object.getAttributeValue("name") + (j > 1 ? "_" + j : ""));

      Element encryptProp = new Element("encrypt", SECURE_NAMESPACE);
      encryptProp.setAttribute("algorithm", object.getAttributeValue("encryptionAlgorithm", "AES"));
      encryptProp.setAttribute("mode", object.getAttributeValue("encryptionMode", "CBC"));

      confProp.addContent(encryptProp);
      object.getDocument().getRootElement().addContent(idx, confProp);

      report.report("configProperties.securePrefix", confProp, confProp);

      ++j;
    }

    if (parseBoolean(object.getAttributeValue("ignoreResourceNotFound", "false"))) {
      report.report("configProperties.ignoreResourceNotFound", object, object);
    }
    if (parseBoolean(object.getAttributeValue("ignoreUnresolvablePlaceholders", "false"))) {
      report.report("configProperties.ignoreUnresolvablePlaceholders", object, object);
    }
    if (!"FALLBACK".equals(object.getAttributeValue("systemPropertiesMode", "FALLBACK"))) {
      report.report("configProperties.systemPropertiesMode", object, object);
    }

    object.detach();
  }

}
