/*
 * Copyright (c) 2015 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.structure.mule.three;

import java.nio.file.Path;

import com.mulesoft.tools.migration.project.structure.mule.MuleProject;

/**
 * @author Mulesoft Inc.
 */
public class MuleApplicationProject extends MuleProject {


  public MuleApplicationProject(Path baseFolder) {
    super(baseFolder);
  }

  @Override
  public Path srcMainConfiguration() {
    return baseFolder.resolve("src").resolve("main").resolve("app");
  }

  @Override
  public Path srcTestConfiguration() {
    return baseFolder.resolve("src").resolve("test").resolve("munit");
  }
}
