/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static org.apache.commons.io.FileUtils.moveDirectory;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.ProjectStructureContribution;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Copy the catalog folder over to keep the reference of the original customTypes.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CopyCatalogFolder implements ProjectStructureContribution {

  @Override
  public String getDescription() {
    return "Copy the catalog folder over to keep the reference of the original customTypes.";
  }

  @Override
  public void execute(Path basePath, MigrationReport report) throws RuntimeException {
    final File sourceCatalogFolder = new File(basePath.toFile(), "catalog");

    if (sourceCatalogFolder.exists()) {
      final File destinationCatalogFolder = new File(basePath.toFile(), "src/main/resources/catalog");

      try {
        moveDirectory(sourceCatalogFolder, destinationCatalogFolder);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

}
