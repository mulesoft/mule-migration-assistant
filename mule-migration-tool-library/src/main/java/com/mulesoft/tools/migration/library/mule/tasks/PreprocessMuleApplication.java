/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.mule.steps.core.SetSecureProperties;
import com.mulesoft.tools.migration.library.mule.steps.core.PreprocessNamespaces;
import com.mulesoft.tools.migration.library.mule.steps.ee.MigrateDWScriptFiles;
import com.mulesoft.tools.migration.library.mule.steps.pom.RemoveBuildHelperMavenPlugin;
import com.mulesoft.tools.migration.library.mule.steps.pom.RemoveMuleAppMavenPlugin;
import com.mulesoft.tools.migration.library.mule.steps.pom.RemoveMuleDependencies;
import com.mulesoft.tools.migration.library.mule.steps.pom.UpdateMuleMavenPlugin;
import com.mulesoft.tools.migration.library.mule.steps.pom.UpdateProjectVersion;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

/**
 * Preprocess Mule Application Migration Task
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class PreprocessMuleApplication extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Preprocess the application";
  }

  @Override
  public String getTo() {
    return MULE_4_VERSION;
  }

  @Override
  public String getFrom() {
    return MULE_3_VERSION;
  }

  @Override
  public List<MigrationStep> getSteps() {
    return newArrayList(new RemoveMuleDependencies(),
                        new UpdateMuleMavenPlugin(),
                        new RemoveMuleAppMavenPlugin(),
                        new RemoveBuildHelperMavenPlugin(),
                        new RemoveMuleDependencies(),
                        new UpdateProjectVersion(),
                        new SetSecureProperties(),
                        new PreprocessNamespaces(),
                        new MigrateDWScriptFiles(),
                        new SetSecureProperties());
  }

}
