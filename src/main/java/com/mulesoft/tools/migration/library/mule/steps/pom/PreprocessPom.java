/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.pom;

import com.mulesoft.tools.migration.engine.step.category.PomContribution;
import com.mulesoft.tools.migration.pom.Dependency;
import com.mulesoft.tools.migration.pom.PomModel;

import java.util.List;

/**
 * Preprocess Mule Application Pom Migration Step
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class PreprocessPom implements PomContribution {

  @Override
  public String getDescription() {
    return "Remove mule dependencies from pom";
  }

  @Override
  public void execute(PomModel pomModel) {
    List<Dependency> dependencies = pomModel.getDependencies();
    dependencies.removeIf(d -> d.getGroupId().startsWith("org.mule") || d.getGroupId().startsWith("com.mulesoft"));
    pomModel.setDependencies(dependencies);
  }
}
