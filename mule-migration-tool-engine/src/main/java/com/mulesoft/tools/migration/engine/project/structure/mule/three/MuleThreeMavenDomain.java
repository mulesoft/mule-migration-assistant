/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.engine.project.structure.mule.three;

import java.nio.file.Path;

/**
 * Represents a mule three domain mavenized project structure
 *
 * @author Mulesoft Inc.
 */
public class MuleThreeMavenDomain extends MuleThreeDomain {

  public MuleThreeMavenDomain(Path baseFolder) {
    super(baseFolder);
  }

  @Override
  public Path srcMainConfiguration() {
    return baseFolder.resolve(srcMainConfigurationPath);
  }
}
