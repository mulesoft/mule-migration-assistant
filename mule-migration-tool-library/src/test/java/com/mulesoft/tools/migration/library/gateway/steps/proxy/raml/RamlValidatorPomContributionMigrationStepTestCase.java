/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.proxy.raml;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.COM_MULESOFT_ANYPOINT_GROUP_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_PLUGIN_CLASSIFIER;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_RAML_VALIDATOR_EXTENSION;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_RAML_VALIDATOR_EXTENSION_VERSION;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_REST_VALIDATOR_EXTENSION;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.junit.Test;

public class RamlValidatorPomContributionMigrationStepTestCase {

  @Test
  public void pomContributionTestWithNoPreviousDependency() {
    PomModel pm = new PomModel();
    RamlValidatorPomContributionMigrationStep step = new RamlValidatorPomContributionMigrationStep();
    step.execute(pm, mock(MigrationReport.class));
    assertThat(pm.getDependencies().size(), is(1));
    Dependency httpPolicyTransformExtension = pm.getDependencies().get(0);
    assertThat(httpPolicyTransformExtension.getGroupId(), is(COM_MULESOFT_ANYPOINT_GROUP_ID));
    assertThat(httpPolicyTransformExtension.getArtifactId(), is(MULE_REST_VALIDATOR_EXTENSION));
    assertThat(httpPolicyTransformExtension.getVersion(), is(notNullValue()));
    assertThat(httpPolicyTransformExtension.getClassifier(), is(MULE_PLUGIN_CLASSIFIER));
  }

  @Test
  public void pomContributionTestWithPreviousDependency() {
    PomModel pm = new PomModel();
    pm.addDependency(getRamlDependency());
    RamlValidatorPomContributionMigrationStep step = new RamlValidatorPomContributionMigrationStep();
    step.execute(pm, mock(MigrationReport.class));
    assertThat(pm.getDependencies().size(), is(1));
    Dependency httpPolicyTransformExtension = pm.getDependencies().get(0);
    assertThat(httpPolicyTransformExtension.getGroupId(), is(COM_MULESOFT_ANYPOINT_GROUP_ID));
    assertThat(httpPolicyTransformExtension.getArtifactId(), is(MULE_REST_VALIDATOR_EXTENSION));
    assertThat(httpPolicyTransformExtension.getVersion(), is(notNullValue()));
    assertThat(httpPolicyTransformExtension.getClassifier(), is(MULE_PLUGIN_CLASSIFIER));
  }

  private Dependency getRamlDependency() {
    return new Dependency.DependencyBuilder()
        .withGroupId(COM_MULESOFT_ANYPOINT_GROUP_ID)
        .withArtifactId(MULE_RAML_VALIDATOR_EXTENSION)
        .withVersion(MULE_RAML_VALIDATOR_EXTENSION_VERSION)
        .withClassifier(MULE_PLUGIN_CLASSIFIER)
        .build();
  }

}
