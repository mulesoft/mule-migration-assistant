/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.steps.project;

import static org.apache.commons.io.FileUtils.deleteDirectory;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.ProjectStructureContribution;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Move api folder to the right location
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RemoveApiApiLocation implements ProjectStructureContribution {

  static final String MULE_4_API_FOLDER = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "api";
  static final String MULE_4_API_DEPENDENCIES_FOLDER = "src" + File.separator + "main" + File.separator + "api-dependencies";

  @Override
  public String getDescription() {
    return "Remove '" + MULE_4_API_FOLDER + "' content'" + MULE_4_API_FOLDER + "'";
  }

  @Override
  public void execute(Path basePath, MigrationReport report) throws RuntimeException {
    final File mule4ApiDependenciesFolder = basePath.resolve(MULE_4_API_DEPENDENCIES_FOLDER).toFile();
    final File mule4ApiFolder = basePath.resolve(MULE_4_API_FOLDER).toFile();
    try {
      if (mule4ApiFolder.exists())
        deleteDirectory(mule4ApiFolder);
      if (mule4ApiDependenciesFolder.exists())
        deleteDirectory(mule4ApiDependenciesFolder);
    } catch (IOException e) {
      throw new RuntimeException("Cannot move api folder", e);
    }
  }
}
