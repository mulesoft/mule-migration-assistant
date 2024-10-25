/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.engine.project.structure.mule;

import com.mulesoft.tools.migration.engine.project.structure.MavenProject;

import java.nio.file.Path;

/**
 * Represents a mule project structure
 *
 * @author Mulesoft Inc.
 */
public abstract class MuleProject extends MavenProject {


  public MuleProject(Path baseFolder) {
    super(baseFolder);
  }

  public abstract Path srcMainConfiguration();

  public abstract Path srcTestConfiguration();
}
