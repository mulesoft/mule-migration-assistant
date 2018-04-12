/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.pom;

import com.mulesoft.tools.migration.step.category.PomContribution;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import org.apache.commons.lang3.StringUtils;

/**
 * Removes the Mule App Maven Plugin from pom
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RemoveMuleAppMavenPlugin implements PomContribution {

  private String MULE_APP_MAVEN_PLUGIN_ARTIFACT_ID = "mule-app-maven-plugin";

  @Override
  public String getDescription() {
    return "Remove mule-app-maven-plugin";
  }

  @Override
  public void execute(PomModel pomModel) throws RuntimeException {
    pomModel.removePlugin(p -> StringUtils.equals(p.getArtifactId(), MULE_APP_MAVEN_PLUGIN_ARTIFACT_ID));
  }
}
