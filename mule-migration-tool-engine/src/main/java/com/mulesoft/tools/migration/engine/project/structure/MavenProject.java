/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
