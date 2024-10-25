/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.step.util;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

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
   * @param fileName         the path of the file
   * @param newFileName      the new path of the file
   * @param applicationModel the {@link ApplicationModel} of the application
   * @param report           the {@link MigrationReport} to update the existing entries on the file
   */
  public static void renameFile(Path fileName, Path newFileName, ApplicationModel applicationModel, MigrationReport report) {
    File fileRename = fileName.toFile();
    if (fileRename.exists()) {
      fileRename.renameTo(newFileName.toFile());
      applicationModel.updateApplicationModelReference(fileName, newFileName);
      report.updateReportEntryFilePath(fileName, newFileName);
    }
  }

  /**
   * Moves a directory.
   * If the destination directory exists it copies the source directory content and then deletes it.
   *
   * @param srcDir the directory to be moved.
   * @param destDir the destination directory.
   * @throws NullPointerException if any of the given {@code File}s are {@code null}.
   * @throws IllegalArgumentException if the source or destination is invalid.
   * @throws IOException if an error occurs.
   */
  public static void moveDirectory(File srcDir, File destDir) throws IOException {
    if (srcDir.isDirectory()) {
      if (!destDir.exists()) {
        FileUtils.moveDirectory(srcDir, destDir);
      } else {
        FileUtils.copyDirectory(srcDir, destDir);
        FileUtils.deleteDirectory(srcDir);
      }
    }
  }

}
