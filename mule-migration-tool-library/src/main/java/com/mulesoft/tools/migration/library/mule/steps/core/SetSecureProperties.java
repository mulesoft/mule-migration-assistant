/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.project.model.artifact.MuleArtifactJsonModelUtils.MULE_ARTIFACT_DEFAULT_CHARSET;
import static java.util.stream.Collectors.toList;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.project.model.artifact.MuleArtifactJsonModel;
import com.mulesoft.tools.migration.project.model.artifact.MuleArtifactJsonModelUtils;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.ProjectStructureContribution;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Add secureProperties to mule-artifact.json based on the mule-app.properties file
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SetSecureProperties implements ProjectStructureContribution {

  public static final String MULE_ARTIFACT_JSON = "mule-artifact.json";
  public static final String SRC_MAIN_RESOURCES_MULE_APP_PROPERTIES = "src/main/resources/mule-app.properties";
  public static final String SECURE_PROPERTIES = "secure.properties";
  public static final String SECURE_PROPERTIES_SEPARATOR = ",";

  @Override
  public String getDescription() {
    return "Update mule-artifact.json with secureProperties in case they are defined in the mule-app.properties.";
  }

  @Override
  public void execute(Path projectBasePath, MigrationReport report) throws RuntimeException {
    try {
      List<String> secureProperties = resolveSecureProperties(projectBasePath);
      if (!secureProperties.isEmpty()) {
        MuleArtifactJsonModel currentModel =
            MuleArtifactJsonModelUtils.buildMuleArtifactJson(projectBasePath.resolve(MULE_ARTIFACT_JSON));

        MuleArtifactJsonModel updatedModel =
            MuleArtifactJsonModelUtils.buildMinimalMuleArtifactJson(currentModel.getMinMuleVersion(), secureProperties);

        FileUtils.write(projectBasePath.resolve(MULE_ARTIFACT_JSON).toFile(), updatedModel.toString(),
                        MULE_ARTIFACT_DEFAULT_CHARSET);
      }
    } catch (IOException e) {
      throw new MigrationStepException("Could not update secureProperties based on mule-app.properties", e);
    }
  }


  private List<String> resolveSecureProperties(Path projectBasePath) throws IOException {
    File muleAppProperties = new File(projectBasePath.toFile(), SRC_MAIN_RESOURCES_MULE_APP_PROPERTIES);
    List<String> secureProperties = new ArrayList<>();
    if (muleAppProperties != null && muleAppProperties.exists()) {
      Properties properties = new Properties();
      properties.load(new FileInputStream(muleAppProperties));
      String securePropertiesList = properties.getProperty(SECURE_PROPERTIES);
      if (securePropertiesList != null) {
        secureProperties =
            Arrays.stream(securePropertiesList.split(SECURE_PROPERTIES_SEPARATOR)).map(String::trim).collect(toList());
        properties.remove(SECURE_PROPERTIES);
        properties.store(new FileOutputStream(muleAppProperties), null);
      }
    }
    return secureProperties;
  }
}
