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
