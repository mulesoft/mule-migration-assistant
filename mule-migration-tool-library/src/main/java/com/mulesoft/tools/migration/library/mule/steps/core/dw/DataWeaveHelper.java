/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.dw;

import static java.lang.System.lineSeparator;
import static org.mule.weave.v2.V2LangMigrant.migrateToV2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Allows migration tasks to generate DW scripts.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class DataWeaveHelper {

  /**
   * @param basePath the migrated project root folder.
   * @return the folder where the migrator should generate any scripts required by the migrated application.
   */
  public static File getMigrationScriptFolder(Path basePath) {
    File migrationScripts = new File(basePath.toFile(), "src/main/resources/migration");
    migrationScripts.mkdirs();
    return migrationScripts;
  }

  public static void scriptWithHeader(File migrationScriptFolder, String scriptName, String outputType, String body)
      throws IOException {
    try (FileWriter writer = new FileWriter(new File(migrationScriptFolder, scriptName))) {
      writer.write("%dw 2.0" + lineSeparator());
      writer.write("output " + outputType + lineSeparator());
      writer.write(" ---" + lineSeparator());

      writer.write(body);
    }

  }

  public static void library(File migrationScriptFolder, String libName, String body)
      throws IOException {
    try (FileWriter writer = new FileWriter(new File(migrationScriptFolder, libName))) {
      writer.write("%dw 2.0" + lineSeparator() + lineSeparator());

      writer.write(body);
    }

  }

  /**
   * @param dwScript dw 1.0 script.
   * @return the dw 2.0 script migrated.
   */
  public static String migrateDWToV2(String dwScript) {
    return migrateToV2(dwScript);
  }
}
