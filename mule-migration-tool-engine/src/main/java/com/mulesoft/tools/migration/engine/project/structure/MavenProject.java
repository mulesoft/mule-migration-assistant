/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.engine.project.structure;

import java.nio.file.Path;

/**
 * Represent a Java Maven project
 * 
 * @author Mulesoft Inc.
 */
public class MavenProject extends JavaProject {

  public MavenProject(Path baseFolder) {
    super(baseFolder);
  }

  public Path pom() {
    return baseFolder.resolve("pom.xml");
  }

}
