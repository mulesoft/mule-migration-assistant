/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.engine.project.structure.mule.four;

import com.mulesoft.tools.migration.engine.project.structure.mule.MuleProject;

import java.io.File;
import java.nio.file.Path;

/**
 * Represents a mule four domain project structure
 *
 * @author Mulesoft Inc.
 */
public class MuleFourDomain extends MuleProject {

  public static final String srcMainConfigurationPath = "src" + File.separator + "main" + File.separator + "mule";
  private static final String MULE_ARTIFACT_JSON = "mule-artifact.json";

  public MuleFourDomain(Path baseFolder) {
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
