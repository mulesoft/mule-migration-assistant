/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.engine.project.structure;

import java.io.File;
import java.nio.file.Path;

/**
 * Represent a Java project
 * 
 * @author Mulesoft Inc.
 */
public class JavaProject extends BasicProject {

  public static final String srcMainJavaPath = "src" + File.separator + "main" + File.separator + "java";
  public static final String srcMainResourcesPath = "src" + File.separator + "main" + File.separator + "resources";
  public static final String srcTestJavaPath = "src" + File.separator + "test" + File.separator + "java";
  public static final String srcTestResourcesPath = "src" + File.separator + "test" + File.separator + "resources";

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
