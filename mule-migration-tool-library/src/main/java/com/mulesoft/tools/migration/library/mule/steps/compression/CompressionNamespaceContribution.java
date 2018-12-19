/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.compression;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.NamespaceContribution;

/**
 * Adds the Compression Module Namespace
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CompressionNamespaceContribution implements NamespaceContribution {

  @Override
  public String getDescription() {
    return "Add the Compression Module namespace";
  }

  @Override
  public void execute(ApplicationModel applicationModel, MigrationReport report) throws RuntimeException {
    if (!applicationModel.getNodes(GZipCompressTransformer.XPATH_SELECTOR).isEmpty() ||
        !applicationModel.getNodes(GZipUncompressTransformer.XPATH_SELECTOR).isEmpty()) {
      applicationModel.addNameSpace("compression", "http://www.mulesoft.org/schema/mule/compression",
                                    "http://www.mulesoft.org/schema/mule/compression/current/mule-compression.xsd");
    }
  }
}
