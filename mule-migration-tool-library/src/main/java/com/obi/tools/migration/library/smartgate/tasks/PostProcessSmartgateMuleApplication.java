/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;
import com.obi.tools.migration.library.smartgate.steps.apikit.ApiKitConfigMigrationStep;
import com.obi.tools.migration.library.smartgate.steps.core.MigrateDefaultExceptionStrategyConfiguration;
import com.obi.tools.migration.library.smartgate.steps.core.PostGlobalsMigrations;
import com.obi.tools.migration.library.smartgate.steps.core.PostSetPayload;
import com.obi.tools.migration.library.smartgate.steps.core.PostSetVariable;
import com.obi.tools.migration.library.smartgate.steps.pom.AddMavenPomContributionMigrationStep;
import com.obi.tools.migration.library.smartgate.steps.pom.RemoveMavenPlugins;
import com.obi.tools.migration.library.smartgate.steps.pom.UpdateMuleDependencies;
import com.obi.tools.migration.library.smartgate.steps.project.RemoveApiApiLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Postprocess Smartgate Mule Application Migration Task
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class PostProcessSmartgateMuleApplication extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Postprocess the Smartgate application";
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
    final ArrayList<MigrationStep> arrayList =
        newArrayList(new PostGlobalsMigrations(), new MigrateDefaultExceptionStrategyConfiguration(),
                     new UpdateMuleDependencies(), new AddMavenPomContributionMigrationStep(), new RemoveMavenPlugins(),
                     new RemoveApiApiLocation(), new ApiKitConfigMigrationStep(), new PostSetVariable(), new PostSetPayload());
    return arrayList;
  }

}
