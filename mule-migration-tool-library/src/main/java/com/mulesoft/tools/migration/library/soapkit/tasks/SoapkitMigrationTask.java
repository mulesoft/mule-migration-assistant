/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.soapkit.tasks;

import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.library.soapkit.steps.SoapkitFault;
import com.mulesoft.tools.migration.library.soapkit.steps.SoapkitHttpListenerMapping;
import com.mulesoft.tools.migration.library.soapkit.steps.SoapkitMigrationTaskPomContribution;
import com.mulesoft.tools.migration.library.soapkit.steps.SoapkitRouter;
import com.mulesoft.tools.migration.library.soapkit.steps.SoapkitRouterConfig;
import com.mulesoft.tools.migration.library.soapkit.steps.SoapkitWsdlLocation;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.ArrayList;
import java.util.List;

import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

/**
 * Migration Task for APIkit for SOAP components
 *
 * @author Mulesoft Inc.
 */
public class SoapkitMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Migrate APIkit for SOAP Components";
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
  public ProjectType getProjectType() {
    return MULE_FOUR_APPLICATION;
  }

  @Override
  public List<MigrationStep> getSteps() {
    List<MigrationStep> steps = new ArrayList<>();

    steps.add(new SoapkitMigrationTaskPomContribution());
    steps.add(new SoapkitRouterConfig());
    steps.add(new SoapkitRouter());
    steps.add(new SoapkitHttpListenerMapping());
    steps.add(new SoapkitWsdlLocation());
    steps.add(new SoapkitFault());
    return steps;
  }
}
