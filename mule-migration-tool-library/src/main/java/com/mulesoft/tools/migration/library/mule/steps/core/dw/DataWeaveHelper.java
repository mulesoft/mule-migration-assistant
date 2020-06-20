/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
