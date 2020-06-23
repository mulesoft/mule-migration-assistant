/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.engine.project.structure.mule.three;

import com.mulesoft.tools.migration.engine.project.structure.mule.MuleProject;

import java.io.File;
import java.nio.file.Path;

/**
 * Represents a mule three application project structure
 *
 * @author Mulesoft Inc.
 */
public class MuleThreeApplication extends MuleProject {

  public static final String srcMainConfigurationPath = "src" + File.separator + "main" + File.separator + "app";
  public static final String srcTestsConfigurationPath = "src" + File.separator + "test" + File.separator + "munit";
  public static final String MULE_APP_PROPERTIES = "mule-app.properties";

  public MuleThreeApplication(Path baseFolder) {
    super(baseFolder);
  }

  @Override
  public Path srcMainConfiguration() {
    return baseFolder.resolve(srcMainConfigurationPath);
  }

  @Override
  public Path srcTestConfiguration() {
    return baseFolder.resolve(srcTestsConfigurationPath);
  }

  public Path appProperties() {
    return srcMainConfiguration().resolve(MULE_APP_PROPERTIES);
  }
}
