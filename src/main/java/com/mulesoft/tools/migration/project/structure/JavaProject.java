/*
 * Copyright (c) 2015 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.structure;

import java.nio.file.Path;

/**
 * Represent a Java project
 * 
 * @author Mulesoft Inc.
 */
public class JavaProject extends BasicProject {

  public JavaProject(Path baseFolder) {
    super(baseFolder);
  }

  public Path srcMainJava() {
    return baseFolder.resolve("src").resolve("main").resolve("java");
  }

  public Path srcMainResources() {
    return baseFolder.resolve("src").resolve("main").resolve("resources");
  }

  public Path srcTestJava() {
    return baseFolder.resolve("src").resolve("test").resolve("java");
  }

  public Path srcTestResources() {
    return baseFolder.resolve("src").resolve("test").resolve("resources");
  }

  // TODO add access to pom file and account for non maven projects

}
