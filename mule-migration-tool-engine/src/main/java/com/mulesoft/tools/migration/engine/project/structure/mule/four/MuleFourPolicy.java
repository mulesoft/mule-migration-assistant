/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.project.structure.mule.four;

import com.mulesoft.tools.migration.engine.project.structure.mule.MuleProject;

import java.io.File;
import java.nio.file.Path;

/**
 * Represents a mule four policy project structure
 *
 * @author Mulesoft Inc.
 */
public class MuleFourPolicy extends MuleProject {

  public static final String srcMainConfigurationPath = "src" + File.separator + "main" + File.separator + "mule";
  private static final String MULE_ARTIFACT_JSON = "mule-artifact.json";

  public MuleFourPolicy(Path baseFolder) {
    super(baseFolder);
  }

  @Override
  public Path srcMainConfiguration() {
    return baseFolder.resolve(srcMainConfigurationPath);
  }

  @Override
  public Path srcTestConfiguration() {
    // TODO throw a better exception
    throw new RuntimeException("No test configuration folder");
  }

  public Path muleArtifactJson() {
    return baseFolder.resolve(MULE_ARTIFACT_JSON);
  }
}
