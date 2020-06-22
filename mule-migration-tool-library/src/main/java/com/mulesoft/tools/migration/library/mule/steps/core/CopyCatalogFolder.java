/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
