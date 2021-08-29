/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.spring;

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
    Element elementToComment = null;
    for (String location : object.getAttributeValue("location").split("\\,")) {
      Element confProp = new Element("configuration-properties", CORE_NAMESPACE);
      confProp.setAttribute("file", location);

      if (object.getAttribute("file-encoding") != null) {
        if (isVersionGreaterOrEquals(getApplicationModel().getMuleVersion(), "4.2.0")) {
          confProp.setAttribute("encoding", object.getAttributeValue("file-encoding"));
        } else {
          report.report("configProperties.encoding", object, object);
        }
      }

      object.getDocument().getRootElement().addContent(idx, confProp);
      if (elementToComment == null) {
        elementToComment = confProp;
      }
    }

    if (object.getAttribute("order") != null) {
      report.report("configProperties.order", object, elementToComment);
    }
    if (object.getAttributeValue("properties-ref") != null
        || parseBoolean(object.getAttributeValue("ignore-resource-not-found", "false"))
        || parseBoolean(object.getAttributeValue("ignore-unresolvable", "false"))
        || parseBoolean(object.getAttributeValue("local-override", "false"))
        || object.getAttributeValue("system-properties-mode") != null) {
      report.report("configProperties.springAttributes", object, elementToComment);
    }

    object.detach();
  }

}
