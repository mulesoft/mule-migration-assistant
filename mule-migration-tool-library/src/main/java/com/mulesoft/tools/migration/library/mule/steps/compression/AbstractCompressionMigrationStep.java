/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.compression;

import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Base class for compression migration steps
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractCompressionMigrationStep extends AbstractApplicationModelMigrationStep {

  protected static Namespace COMPRESSION_NAMESPACE =
      getNamespace("compression", "http://www.mulesoft.org/schema/mule/compression");

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    element.setNamespace(COMPRESSION_NAMESPACE);
    element.setName(getOperationName());
    element.addContent(getStrategyElement(getStrategyName()));

    transformArguments(element, report);
  }

  protected abstract String getStrategyName();

  protected abstract String getOperationName();

  protected Element getStrategyElement(String name) {
    Element element = new Element(name, COMPRESSION_NAMESPACE);
    element.addContent(new Element("gzip-" + name, COMPRESSION_NAMESPACE));

    return element;
  }

  protected void transformArguments(Element element, MigrationReport report) {
    element.removeAttribute("name");
    removeArgumentAndReport(element, "returnClass", "compression.returnClass", report);
    removeArgumentAndReport(element, "ignoreBadInput", "compression.ignoreBadInput", report);
  }

  protected void removeArgumentAndReport(Element element, String argumentName, String reportEntryKey, MigrationReport report) {
    Attribute attribute = element.getAttribute(argumentName);
    if (attribute != null) {
      report.report(reportEntryKey, element, element);
      element.removeAttribute(attribute);
    }
  }
}
