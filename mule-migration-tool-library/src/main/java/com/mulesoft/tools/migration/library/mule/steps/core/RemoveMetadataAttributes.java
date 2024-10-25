/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static java.util.stream.Collectors.toList;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Remove metadata:id attribute that is used for custom types in studio.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RemoveMetadataAttributes extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "//*[@*[namespace-uri() = 'http://www.mulesoft.org/schema/mule/metadata']]";
  public static final Namespace METADATA_NAMESPACE = getNamespace("metadata", "http://www.mulesoft.org/schema/mule/metadata");

  @Override
  public String getDescription() {
    return "Remove metadata:id attribute.";
  }

  public RemoveMetadataAttributes() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    report.report("transform.studioCustomTypes", element, element);
    element.getAttributes()
        .stream()
        .filter(att -> att.getNamespace().equals(METADATA_NAMESPACE))
        .collect(toList())
        .forEach(att -> att.detach());
    element.removeNamespaceDeclaration(METADATA_NAMESPACE);
  }

  @Override
  public boolean shouldReportMetrics() {
    return false;
  }
}
