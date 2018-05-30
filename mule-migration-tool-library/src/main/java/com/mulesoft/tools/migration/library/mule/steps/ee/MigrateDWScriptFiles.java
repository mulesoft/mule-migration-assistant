/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.ee;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.ProjectStructureContribution;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.migrateDWToV2;

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
    dwFiles.forEach(f -> migrateFile(f));
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
