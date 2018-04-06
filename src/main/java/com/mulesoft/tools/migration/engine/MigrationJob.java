/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine;


import static com.google.common.base.Preconditions.checkState;
import static com.mulesoft.tools.migration.project.structure.ProjectType.BASIC;
import static com.mulesoft.tools.migration.project.structure.ProjectType.JAVA;
import static com.mulesoft.tools.migration.project.structure.ProjectType.MULE_FOUR_APPLICATION;
import static com.mulesoft.tools.migration.project.structure.ProjectType.MULE_THREE_APPLICATION;
import static com.mulesoft.tools.migration.project.structure.ProjectType.MULE_THREE_DOMAIN;
import static com.mulesoft.tools.migration.project.structure.ProjectType.MULE_THREE_MAVEN_APPLICATION;
import static com.mulesoft.tools.migration.project.structure.ProjectType.MULE_THREE_MAVEN_DOMAIN;

import com.mulesoft.tools.migration.engine.exception.MigrationJobException;
import com.mulesoft.tools.migration.engine.exception.MigrationTaskException;
import com.mulesoft.tools.migration.engine.structure.ApplicationPersister;
import com.mulesoft.tools.migration.engine.task.AbstractMigrationTask;
import com.mulesoft.tools.migration.project.ProjectTypeFactory;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.ApplicationModel.ApplicationModelBuilder;
import com.mulesoft.tools.migration.project.structure.ProjectType;
import com.mulesoft.tools.migration.project.structure.mule.MuleProject;
import com.mulesoft.tools.migration.project.structure.mule.four.MuleFourApplication;
import com.mulesoft.tools.migration.project.structure.mule.four.MuleFourDomain;
import com.mulesoft.tools.migration.project.structure.mule.three.MuleThreeApplication;
import com.mulesoft.tools.migration.project.structure.mule.three.MuleThreeDomain;
import com.mulesoft.tools.migration.project.structure.mule.three.MuleThreeMavenApplication;
import com.mulesoft.tools.migration.project.structure.mule.three.MuleThreeMavenDomain;
import com.mulesoft.tools.migration.report.ReportingStrategy;
import com.mulesoft.tools.migration.report.console.ConsoleReportStrategy;
import com.mulesoft.tools.migration.report.html.HTMLReportStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

/**
 * It represent a migration job which is composed by one or more {@link AbstractMigrationTask}
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MigrationJob implements Executable {

  private transient Logger logger = LoggerFactory.getLogger(this.getClass());
  private static ProjectTypeFactory projectFactory = new ProjectTypeFactory();

  private ReportingStrategy reportingStrategy;
  private Path project;
  private Path outputProject;
  private List<AbstractMigrationTask> migrationTasks;

  private MigrationJob(Path project, Path outputProject, List<AbstractMigrationTask> migrationTasks,
                       ReportingStrategy reportingStrategy) {
    this.project = project;
    this.outputProject = outputProject;

    this.migrationTasks = migrationTasks;

    this.reportingStrategy = reportingStrategy;
  }

  @Override
  public void execute() throws Exception {
    ApplicationModel applicationModel = generateApplicationModel(project);
    for (AbstractMigrationTask task : migrationTasks) {
      task.setApplicationModel(applicationModel);
      try {
        task.execute();
        persistApplicationModel(applicationModel);
        // TODO support domains migration
        applicationModel = generateApplicationModel(outputProject, MULE_FOUR_APPLICATION);
      } catch (MigrationTaskException ex) {
        logger.error("Failed to apply task, rolling back and continuing with the next one.");
      } catch (Exception e) {
        throw new MigrationJobException("Failed to continue executing migration: " + e.getMessage(), e);
      }
    }
    generateReport();
  }

  private void persistApplicationModel(ApplicationModel applicationModel) throws Exception {
    ApplicationPersister persister = new ApplicationPersister(applicationModel, outputProject);
    persister.persist();
  }

  private ApplicationModel generateApplicationModel(Path project) throws Exception {
    MuleProject muleProject = null;
    ProjectType type = projectFactory.getProjectType(project);
    if (!type.equals(BASIC) && !type.equals(JAVA)) {
      if (type.equals(MULE_THREE_APPLICATION)) {
        muleProject = new MuleThreeApplication(project);
      } else if (type.equals(MULE_THREE_MAVEN_APPLICATION)) {
        muleProject = new MuleThreeMavenApplication(project);
      } else if (type.equals(MULE_THREE_DOMAIN)) {
        muleProject = new MuleThreeDomain(project);
      } else if (type.equals(MULE_THREE_MAVEN_DOMAIN)) {
        muleProject = new MuleThreeMavenDomain(project);
      }
    }
    return new ApplicationModelBuilder(muleProject).build();
  }

  private ApplicationModel generateApplicationModel(Path project, ProjectType type) throws Exception {
    if (type.equals(MULE_FOUR_APPLICATION)) {
      return new ApplicationModelBuilder(new MuleFourApplication(project)).build();
    } else {
      return new ApplicationModelBuilder(new MuleFourDomain(project)).build();
    }
  }

  private void generateReport() {
    // TODO this GOES TO ANOTHER CLASS
    if (reportingStrategy instanceof HTMLReportStrategy) {
      ((HTMLReportStrategy) this.reportingStrategy).generateReport();
    }
  }

  /**
   * It represent the builder to obtain a {@link MigrationJob}
   *
   * @author Mulesoft Inc.
   * @since 1.0.0
   */
  public static class MigrationJobBuilder {

    private Path project;
    private Path outputProject;
    private List<AbstractMigrationTask> migrationTasks;

    private ReportingStrategy reportingStrategy = new ConsoleReportStrategy();

    public MigrationJobBuilder withProject(Path project) {
      this.project = project;
      return this;
    }

    public MigrationJobBuilder withOutputProject(Path outputProject) {
      this.outputProject = outputProject;
      return this;
    }

    public MigrationJobBuilder withMigrationTasks(List<AbstractMigrationTask> migrationTasks) {
      this.migrationTasks = migrationTasks;
      return this;
    }

    public MigrationJobBuilder withReportingStrategy(ReportingStrategy reportingStrategy) {
      this.reportingStrategy = reportingStrategy;
      return this;
    }

    public MigrationJob build() {
      checkState(project != null, "The project must not be null");
      checkState(outputProject != null, "The output project must not be null");
      checkState(migrationTasks != null, "The migration task  must not be null");

      return new MigrationJob(project, outputProject, migrationTasks, reportingStrategy);
    }
  }

}
