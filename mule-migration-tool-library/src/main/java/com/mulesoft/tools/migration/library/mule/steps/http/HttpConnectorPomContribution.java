/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import com.mulesoft.tools.migration.step.category.PomContribution;
import com.mulesoft.tools.migration.project.model.pom.Dependency.DependencyBuilder;
import com.mulesoft.tools.migration.project.model.pom.PomModel;

/**
 * Adds the HTTP Connector dependency
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpConnectorPomContribution implements PomContribution {

  @Override
  public String getDescription() {
    return "Add HTTP Connector dependency.";
  }

  @Override
  public void execute(PomModel object) throws RuntimeException {
    object.addDependency(new DependencyBuilder()
        .withGroupId("org.mule.connectors")
        .withArtifactId("mule-http-connector")
        .withVersion("1.2.0")
        .withClassifier("mule-plugin")
        .build());
  }

}
