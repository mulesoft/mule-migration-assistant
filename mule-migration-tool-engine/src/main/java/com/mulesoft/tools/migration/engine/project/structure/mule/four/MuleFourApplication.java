/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.engine.project.structure.mule.four;

import com.mulesoft.tools.migration.engine.project.structure.mule.MuleProject;

import java.io.File;
import java.nio.file.Path;

/**
 * Represents a mule four application project structure
 *
 * @author Mulesoft Inc.
 */
public class MuleFourApplication extends MuleProject {

  public static final String srcMainConfigurationPath = "src" + File.separator + "main" + File.separator + "mule";
  public static final String srcTestConfigurationPath = "src" + File.separator + "test" + File.separator + "munit";
  private static final String MULE_ARTIFACT_JSON = "mule-artifact.json";

  public MuleFourApplication(Path baseFolder) {
    super(baseFolder);
  }

  @Override
  public Path srcMainConfiguration() {
    return baseFolder.resolve(srcMainConfigurationPath);
  }

  @Override
  public Path srcTestConfiguration() {
    return baseFolder.resolve(srcTestConfigurationPath);
  }

  public Path muleArtifactJson() {
    return baseFolder.resolve(MULE_ARTIFACT_JSON);
  }
}
