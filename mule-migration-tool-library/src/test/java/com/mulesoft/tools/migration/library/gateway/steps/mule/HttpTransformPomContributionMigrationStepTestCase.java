/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.mule;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.COM_MULESOFT_ANYPOINT_GROUP_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_HTTP_POLICY_TRANSFORM_EXTENSION_ARTIFACT_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_PLUGIN_CLASSIFIER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.library.gateway.steps.policy.mule.HttpTransformPomContributionMigrationStep;
import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.junit.Test;

public class HttpTransformPomContributionMigrationStepTestCase {

  @Test
  public void pomContributionTest() {
    PomModel pm = new PomModel();
    HttpTransformPomContributionMigrationStep step = new HttpTransformPomContributionMigrationStep();
    step.execute(pm, mock(MigrationReport.class));
    assertThat(pm.getDependencies().size(), is(1));
    Dependency httpPolicyTransformExtension = pm.getDependencies().get(0);
    assertThat(httpPolicyTransformExtension.getGroupId(), is(COM_MULESOFT_ANYPOINT_GROUP_ID));
    assertThat(httpPolicyTransformExtension.getArtifactId(), is(MULE_HTTP_POLICY_TRANSFORM_EXTENSION_ARTIFACT_ID));
    assertThat(httpPolicyTransformExtension.getVersion(), is(notNullValue()));
    assertThat(httpPolicyTransformExtension.getClassifier(), is(MULE_PLUGIN_CLASSIFIER));
  }
}
