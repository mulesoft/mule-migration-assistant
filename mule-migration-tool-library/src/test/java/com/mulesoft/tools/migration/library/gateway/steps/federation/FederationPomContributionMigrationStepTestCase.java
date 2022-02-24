/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.federation;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.COM_MULESOFT_ANYPOINT_GROUP_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_FEDERATION_EXTENSION_ARTIFACT_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_HTTP_POLICY_TRANSFORM_EXTENSION_ARTIFACT_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_PLUGIN_CLASSIFIER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.library.gateway.steps.policy.federation.FederationPomContributionMigrationStep;
import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.junit.Test;

public class FederationPomContributionMigrationStepTestCase {

  @Test
  public void pomContributionTest() {
    MigrationReport reportMock = reportMock = mock(MigrationReport.class);
    PomModel pm = new PomModel();
    FederationPomContributionMigrationStep step = new FederationPomContributionMigrationStep();
    step.execute(pm, reportMock);
    assertThat(pm.getDependencies().size(), is(2));
    Dependency httpPolicyTransformExtension = pm.getDependencies().get(0);
    assertThat(httpPolicyTransformExtension.getGroupId(), is(COM_MULESOFT_ANYPOINT_GROUP_ID));
    assertThat(httpPolicyTransformExtension.getArtifactId(), is(MULE_HTTP_POLICY_TRANSFORM_EXTENSION_ARTIFACT_ID));
    assertThat(httpPolicyTransformExtension.getVersion(), is(notNullValue()));
    assertThat(httpPolicyTransformExtension.getClassifier(), is(MULE_PLUGIN_CLASSIFIER));
    Dependency federationExtensionDependency = pm.getDependencies().get(1);
    assertThat(federationExtensionDependency.getGroupId(), is(COM_MULESOFT_ANYPOINT_GROUP_ID));
    assertThat(federationExtensionDependency.getArtifactId(), is(MULE_FEDERATION_EXTENSION_ARTIFACT_ID));
    assertThat(federationExtensionDependency.getVersion(), is(notNullValue()));
    assertThat(federationExtensionDependency.getClassifier(), is(MULE_PLUGIN_CLASSIFIER));
  }
}
