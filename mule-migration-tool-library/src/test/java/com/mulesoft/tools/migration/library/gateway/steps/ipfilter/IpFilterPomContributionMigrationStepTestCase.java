/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.ipfilter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.mulesoft.tools.migration.library.gateway.steps.policy.ipfilter.IpFilterPomContributionMigrationStep;
import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;

import org.jdom2.Element;
import org.junit.Test;

public class IpFilterPomContributionMigrationStepTestCase extends AbstractIpFilterMigrationTestCase {

  private static final String COM_MULESOFT_ANYPOINT_GROUP_ID = "com.mulesoft.anypoint";
  private static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";
  private static final String MULE_IP_FILTER_EXTENSION_ARTIFACT_ID = "mule-ip-filter-extension";
  private static final String EXTENSION_VERSION = "1.1.0";

  @Override
  protected Element getTestElement() {
    return null;
  }

  @Test
  public void pomContributionTest() {
    PomModel pm = new PomModel();
    IpFilterPomContributionMigrationStep step = new IpFilterPomContributionMigrationStep();
    step.execute(pm, reportMock);
    assertThat(pm.getDependencies().size(), is(1));
    Dependency ipFilterExtension = pm.getDependencies().get(0);
    assertThat(ipFilterExtension.getGroupId(), is(COM_MULESOFT_ANYPOINT_GROUP_ID));
    assertThat(ipFilterExtension.getArtifactId(), is(MULE_IP_FILTER_EXTENSION_ARTIFACT_ID));
    assertThat(ipFilterExtension.getVersion(), is(EXTENSION_VERSION));
    assertThat(ipFilterExtension.getClassifier(), is(MULE_PLUGIN_CLASSIFIER));
  }
}
