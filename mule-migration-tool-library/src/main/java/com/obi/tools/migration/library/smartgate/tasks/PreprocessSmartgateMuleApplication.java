/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import java.util.List;

import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;
import com.obi.tools.migration.library.smartgate.steps.pom.AddSmartgateAPIPomContribution;
import com.obi.tools.migration.library.smartgate.steps.pom.AddSmartgateAndMuleDependencies;
import com.obi.tools.migration.library.smartgate.steps.pom.RemoveMuleRepositories;
import com.obi.tools.migration.library.smartgate.steps.pom.RemoveSmartgateDependencies;
import com.obi.tools.migration.library.smartgate.steps.pom.SetSmartgateProjectDescription;
import com.obi.tools.migration.library.smartgate.steps.pom.SmartgateExceptionStrategyRef;
import com.obi.tools.migration.library.smartgate.steps.pom.UpdateProjectParent;
import com.obi.tools.migration.library.smartgate.steps.pom.UpdateProjectProperties;
import com.obi.tools.migration.library.smartgate.steps.properties.SmartgateStageAppProperties;

/**
 * Preprocess Smartgate Mule Application Migration Task
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class PreprocessSmartgateMuleApplication extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Preprocess the Smartgate application";
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
    return newArrayList(new AddSmartgateAPIPomContribution(), new RemoveMuleRepositories(), new RemoveSmartgateDependencies(),
                        new SetSmartgateProjectDescription(), new UpdateProjectParent(), new UpdateProjectProperties(),
                        new AddSmartgateAndMuleDependencies(), new SmartgateExceptionStrategyRef());
  }

}
