/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.ee;

import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.migrateDWToV2;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.ProjectStructureContribution;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Search for all .dwl files on app and migrate them to DW v2.0
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MigrateDWScriptFiles implements ProjectStructureContribution {

  @Override
  public String getDescription() {
    return "Migrate .dwl files to DW v2.0.";
  }

  @Override
  public void execute(Path basePath, MigrationReport report) throws RuntimeException {
    String[] extensions = new String[] {"dwl"};
    List<File> dwFiles = (List<File>) FileUtils.listFiles(basePath.toFile(), extensions, true);
    dwFiles.forEach(f -> {
      try {
        migrateFile(f);
      } catch (Exception ex) {
        report.report("dataWeave.migrationErrorFile", null, null, f.getPath(), ex.getMessage());
      }
    });
  }

  private void migrateFile(File file) {
    try {
      String dwScript = new String(Files.readAllBytes(file.toPath()));
      dwScript = migrateDWToV2(dwScript);
      Files.write(file.toPath(), dwScript.getBytes());
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }

  }
}
