/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
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

  public static final String srcMainJavaPath = "src/main/java";
  public static final String srcMainResourcesPath = "src/main/resources";
  public static final String srcTestJavaPath = "src/test/java";
  public static final String srcTestResourcesPath = "src/test/resources";

  public JavaProject(Path baseFolder) {
    super(baseFolder);
  }

  public Path srcMainJava() {
    return baseFolder.resolve(srcMainJavaPath);
  }

  public Path srcMainResources() {
    return baseFolder.resolve(srcMainResourcesPath);
  }

  public Path srcTestJava() {
    return baseFolder.resolve(srcTestJavaPath);
  }

  public Path srcTestResources() {
    return baseFolder.resolve(srcTestResources());
  }

  // TODO add access to pom file and account for non maven projects

}
