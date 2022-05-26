/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.apikit.steps;

import static com.mulesoft.tools.migration.step.util.ProjectStructureUtils.moveDirectory;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.ProjectStructureContribution;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Move api folder to the right location
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ApikitApiLocation implements ProjectStructureContribution {

  static final String MULE_3_API_FOLDER = "src" + File.separator + "main" + File.separator + "api";
  static final String MULE_4_API_FOLDER = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "api";

  @Override
  public String getDescription() {
    return "Move '" + MULE_3_API_FOLDER + "' content to '" + MULE_4_API_FOLDER + "'";
  }

  @Override
  public void execute(Path basePath, MigrationReport report) throws RuntimeException {
    final File mule3ApiFolder = basePath.resolve(MULE_3_API_FOLDER).toFile();
    final File mule4ApiFolder = basePath.resolve(MULE_4_API_FOLDER).toFile();
    try {
      if (mule3ApiFolder.exists())
        moveDirectory(mule3ApiFolder, mule4ApiFolder);
    } catch (IOException e) {
      throw new RuntimeException("Cannot move api folder", e);
    }
  }
}
