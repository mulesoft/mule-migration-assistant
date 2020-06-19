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
