/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.structure.mule.three;

import com.mulesoft.tools.migration.project.structure.mule.MuleProject;

import java.nio.file.Path;

/**
 * Represents a mule three application project structure
 *
 * @author Mulesoft Inc.
 */
public class MuleThreeApplication extends MuleProject {

  public static final String srcMainConfigurationPath = "src/main/app";
  public static final String srcTestsConfigurationPath = "src/test/munit";

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
}
