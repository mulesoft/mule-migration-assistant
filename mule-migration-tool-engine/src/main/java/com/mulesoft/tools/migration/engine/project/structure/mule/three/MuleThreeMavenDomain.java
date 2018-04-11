/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
