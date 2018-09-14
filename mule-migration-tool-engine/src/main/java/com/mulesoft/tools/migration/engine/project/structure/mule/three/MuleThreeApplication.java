/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
