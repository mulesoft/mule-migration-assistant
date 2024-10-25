/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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
