/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.mule.steps.spring.AuthorizationFilter;
import com.mulesoft.tools.migration.library.mule.steps.spring.ExportSpringBeanPackages;
import com.mulesoft.tools.migration.library.mule.steps.spring.SecurityManager;
import com.mulesoft.tools.migration.library.mule.steps.spring.SpringBeans;
import com.mulesoft.tools.migration.library.mule.steps.spring.SpringConfigContainingMuleConfig;
import com.mulesoft.tools.migration.library.mule.steps.spring.SpringConfigInMuleConfig;
import com.mulesoft.tools.migration.library.mule.steps.spring.SpringContext;
import com.mulesoft.tools.migration.library.mule.steps.spring.SpringContributions;
import com.mulesoft.tools.migration.library.mule.steps.spring.SpringPomContribution;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;
import com.mulesoft.tools.migration.task.Version;

import java.util.List;

/**
 * Migrate Spring bean definitions
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SpringMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Migrate Spring bean definitions";
  }

  @Override
  public Version getTo() {
    return MULE_4_VERSION;
  }

  @Override
  public Version getFrom() {
    return MULE_3_VERSION;
  }

  @Override
  public ProjectType getProjectType() {
    return MULE_FOUR_APPLICATION;
  }

  @Override
  public List<MigrationStep> getSteps() {
    return newArrayList(new SpringPomContribution(),
                        new SpringConfigInMuleConfig(),
                        new SpringConfigContainingMuleConfig(),
                        new SpringBeans(),
                        new SpringContext(),
                        new SecurityManager(),
                        new AuthorizationFilter(),
                        new SpringContributions(),
                        new ExportSpringBeanPackages());
  }
}
