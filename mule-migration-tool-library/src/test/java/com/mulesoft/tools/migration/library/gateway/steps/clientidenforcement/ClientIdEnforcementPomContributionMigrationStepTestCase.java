/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.clientidenforcement;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.COM_MULESOFT_ANYPOINT_GROUP_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_CLIENT_ID_ENFORCEMENT_EXTENSION_ARTIFACT_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_PLUGIN_CLASSIFIER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.library.gateway.steps.policy.clientidenforcement.ClientIdEnforcementPomContributionMigrationStep;
import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.junit.Test;

public class ClientIdEnforcementPomContributionMigrationStepTestCase {

  @Test
  public void pomContributionTest() {
    PomModel pm = new PomModel();
    ClientIdEnforcementPomContributionMigrationStep step = new ClientIdEnforcementPomContributionMigrationStep();
    step.execute(pm, mock(MigrationReport.class));
    assertThat(pm.getDependencies().size(), is(1));
    Dependency clientIdEnforcementDependency = pm.getDependencies().get(0);
    assertThat(clientIdEnforcementDependency.getGroupId(), is(COM_MULESOFT_ANYPOINT_GROUP_ID));
    assertThat(clientIdEnforcementDependency.getArtifactId(), is(MULE_CLIENT_ID_ENFORCEMENT_EXTENSION_ARTIFACT_ID));
    assertThat(clientIdEnforcementDependency.getVersion(), is(notNullValue()));
    assertThat(clientIdEnforcementDependency.getClassifier(), is(MULE_PLUGIN_CLASSIFIER));
  }

}
