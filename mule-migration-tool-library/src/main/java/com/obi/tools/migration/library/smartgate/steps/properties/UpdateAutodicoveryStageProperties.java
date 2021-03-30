/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.steps.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.NamespaceContribution;

/**
 * Migrate api.autodiscovery.version and api.autodiscovery.name in Stage properties
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class UpdateAutodicoveryStageProperties implements NamespaceContribution {


  private static final String API_AUTODISCOVERY_NAME = "api.autodiscovery.name";
  private static final String API_AUTODISCOVERY_VERSION = "api.autodiscovery.version";
  private static final String SMARGATE_STAGE_PROD_FILE_NAME = "prod.properties";
  private static final String SMARTGATE_FILE_PATH =
      "src" + File.separator + "main" + File.separator + "resources" + File.separator + SMARGATE_STAGE_PROD_FILE_NAME;

  @Override
  public String getDescription() {
    return "Migrate api.autodiscovery.version and api.autodiscovery.name in Stage properties";
  }

  @Override
  public void execute(ApplicationModel appModel, MigrationReport report) throws RuntimeException {
    try {
      Properties properties = resolveProperties(appModel.getProjectBasePath(), SMARTGATE_FILE_PATH);
      String version = properties.getProperty(API_AUTODISCOVERY_VERSION, null);
      if (version != null) {
        properties.setProperty(API_AUTODISCOVERY_VERSION, "rainer");
      }

      String name = properties.getProperty(API_AUTODISCOVERY_NAME);
      if (name != null) {
        Optional<PomModel> pomModel = appModel.getPomModel();
        properties.setProperty(API_AUTODISCOVERY_NAME, pomModel.get().getArtifactId());
      }
      writeProerties(properties, appModel.getProjectBasePath(), SMARTGATE_FILE_PATH);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void writeProerties(Properties properties, Path filePath, String propsFileName) throws IOException {



    File muleAppProperties = new File(filePath.toFile(), propsFileName);
    FileOutputStream out = new FileOutputStream(muleAppProperties);
    try {
      properties.store(out, null);
    } finally {
      out.close();
    }

  }

  private Properties resolveProperties(Path filePath, String propsFileName) throws IOException {
    File muleAppProperties = new File(filePath.toFile(), propsFileName);
    Properties properties = new Properties();
    if (muleAppProperties != null && muleAppProperties.exists()) {
      properties.load(new FileInputStream(muleAppProperties));
    }
    return properties;
  }
}
