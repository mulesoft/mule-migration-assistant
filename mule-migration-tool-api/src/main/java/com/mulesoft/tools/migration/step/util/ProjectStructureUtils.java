/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.step.util;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.io.File;
import java.nio.file.Path;

/**
 * Provides reusable methods for common migration scenarios.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public final class ProjectStructureUtils {

  /**
   * Method to rename/move an existing file on the application
   *
   * @param fileName the path of the file
   * @param newFileName the new path of the file
   * @param applicationModel the {@link ApplicationModel} of the application
   * @param report the {@link MigrationReport} to update the existing entries on the file
   */
  public static void renameFile(Path fileName, Path newFileName, ApplicationModel applicationModel, MigrationReport report) {
    File fileRename = fileName.toFile();
    if (fileRename.exists()) {
      fileRename.renameTo(newFileName.toFile());
      applicationModel.updateApplicationModelReference(fileName, newFileName);
      report.updateReportEntryFilePath(fileName, newFileName);
    }
  }
}
