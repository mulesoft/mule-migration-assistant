/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.steps.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;
import org.apache.commons.configuration2.ex.ConfigurationException;

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


  private static final String API_CONSOLE_ENABLED = "api.console.enabled";
  private static final String API_AUTODISCOVERY_NAME = "api.autodiscovery.name";
  private static final String API_AUTODISCOVERY_VERSION = "api.autodiscovery.version";
  private static final List<String> SMARGATE_STAGE_PROD_FILE_NAME =
      Arrays.asList("prod.properties", "fqa.properties", "fqa.properties", "devtest.properties");
  private static final String SMARTGATE_FILE_PATH = "src" + File.separator + "main" + File.separator + "resources";

  @Override
  public String getDescription() {
    return "Migrate api.autodiscovery.version and api.autodiscovery.name in Stage properties";
  }

  @Override
  public void execute(ApplicationModel appModel, MigrationReport report) throws RuntimeException {

    for (String stageFileName : SMARGATE_STAGE_PROD_FILE_NAME) {
      modifyPropertiesFile(appModel, stageFileName);
    }
  }

  private void modifyPropertiesFile(ApplicationModel appModel, String stageFileName) {
    try {

      File file = new File(appModel.getProjectBasePath().toFile(), SMARTGATE_FILE_PATH + File.separator + stageFileName);
      PropertiesConfiguration config = new PropertiesConfiguration();
      PropertiesConfigurationLayout layout = new PropertiesConfigurationLayout();
      config.setLayout(layout);
      layout.load(config, new InputStreamReader(new FileInputStream(file)));

      // Search and modify API_AUTODISCOVERY_VERSION
      String version = (String) config.getProperty(API_AUTODISCOVERY_VERSION);
      if (version != null) {
        version = version.replaceAll("v1:", "");
        config.setProperty(API_AUTODISCOVERY_VERSION, version);
      }

      // Search and modify API_AUTODISCOVERY_NAME
      String name = (String) config.getProperty(API_AUTODISCOVERY_NAME);
      if (name != null) {
        Optional<PomModel> pomModel =
            appModel.getPomModel();
        config.setProperty(API_AUTODISCOVERY_NAME, pomModel.get().getArtifactId());
      }

      // Remove if exits
      Object console = config.getProperty(API_AUTODISCOVERY_NAME);
      if (console != null) {
        config.clearProperty(API_CONSOLE_ENABLED);
      }


      layout.save(config, new FileWriter(file, false));

    } catch (IOException | ConfigurationException e) {
      throw new RuntimeException("Unable to modify File: " + stageFileName, e);
    }
  }
}
