/*
 * Copyright (c) 2015 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.structure.mule.four;

import java.nio.file.Path;
import com.mulesoft.tools.migration.project.structure.JavaProject;

/**
 * @author Mulesoft Inc.
 */
public class MuleApplication extends JavaProject {

  public MuleApplication(Path baseFolder) {
    super(baseFolder);
  }
}
