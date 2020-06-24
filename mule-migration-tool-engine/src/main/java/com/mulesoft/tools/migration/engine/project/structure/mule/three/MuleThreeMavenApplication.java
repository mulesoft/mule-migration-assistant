/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.engine.project.structure.mule.three;

import java.nio.file.Path;

/**
 * Represents a mule three application mavenized project structure
 *
 * @author Mulesoft Inc.
 */
public class MuleThreeMavenApplication extends MuleThreeApplication {

  public MuleThreeMavenApplication(Path baseFolder) {
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
