/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine;


import static com.google.common.base.Preconditions.checkState;
import static com.mulesoft.tools.migration.engine.project.MuleProjectFactory.getMuleProject;
import static com.mulesoft.tools.migration.engine.project.structure.BasicProject.getFiles;
import static com.mulesoft.tools.migration.library.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.library.util.MuleVersion.MULE_4_VERSION;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;

import com.mulesoft.tools.migration.Executable;
import com.mulesoft.tools.migration.engine.exception.MigrationJobException;
import com.mulesoft.tools.migration.engine.project.structure.ApplicationPersister;
import com.mulesoft.tools.migration.engine.project.structure.mule.MuleProject;
import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourApplication;
import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourDomain;
import com.mulesoft.tools.migration.exception.MigrationTaskException;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.ApplicationModel.ApplicationModelBuilder;
import com.mulesoft.tools.migration.report.ReportingStrategy;
import com.mulesoft.tools.migration.report.console.ConsoleReportStrategy;
import com.mulesoft.tools.migration.report.html.HTMLReportStrategy;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import com.mulesoft.tools.migration.task.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * It represent a migration job which is composed by one or more {@link AbstractMigrationTask}
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MigrationJob implements Executable {

  private transient Logger logger = LoggerFactory.getLogger(this.getClass());

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
  public void execute(MigrationReport report) throws Exception {
    ApplicationModel applicationModel = generateApplicationModel(project);
    persistApplicationModel(applicationModel);
    applicationModel = generateApplicationModel(outputProject, MULE_FOUR_APPLICATION);
    for (AbstractMigrationTask task : migrationTasks) {
      task.setApplicationModel(applicationModel);
      try {
        task.execute(report);
        persistApplicationModel(applicationModel);
        // TODO support domains migration
        applicationModel = generateApplicationModel(outputProject, MULE_FOUR_APPLICATION);
      } catch (MigrationTaskException ex) {
        logger.error("Failed to apply task, rolling back and continuing with the next one.", ex);
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
    MuleProject muleProject = getMuleProject(project);
    ApplicationModelBuilder builder = new ApplicationModelBuilder()
        .withConfigurationFiles(getFiles(muleProject.srcMainConfiguration()))
        .withPom(muleProject.pom())
        .withProjectBasePath(muleProject.getBaseFolder());
    if (muleProject.srcTestConfiguration().toFile().exists()) {
      builder.withTestConfigurationFiles(getFiles(muleProject.srcTestConfiguration()));
    }
    return builder.build();
  }

  private ApplicationModel generateApplicationModel(Path project, ProjectType type) throws Exception {
    if (type.equals(MULE_FOUR_APPLICATION)) {
      MuleFourApplication application = new MuleFourApplication(project);
      return new ApplicationModelBuilder()
          .withConfigurationFiles(getFiles(application.srcMainConfiguration()))
          .withTestConfigurationFiles(getFiles(application.srcTestConfiguration()))
          .withMuleArtifactJson(application.muleArtifactJson())
          .withProjectBasePath(application.getBaseFolder())
          .withPom(application.pom()).build();
    } else {
      MuleFourDomain domain = new MuleFourDomain(project);
      return new ApplicationModelBuilder()
          .withConfigurationFiles(getFiles(domain.srcMainConfiguration()))
          .withTestConfigurationFiles(getFiles(domain.srcTestConfiguration()))
          .withProjectBasePath(domain.getBaseFolder())
          .withPom(domain.pom()).build();
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
    private ProjectType outputProjectType;
    private Version inputVersion;
    private Version outputVersion;
    private List<AbstractMigrationTask> migrationTasks = new ArrayList<>();

    private ReportingStrategy reportingStrategy = new ConsoleReportStrategy();

    public MigrationJobBuilder withProject(Path project) {
      this.project = project;
      return this;
    }

    public MigrationJobBuilder withOutputProject(Path outputProject) {
      this.outputProject = outputProject;
      return this;
    }

    public MigrationJobBuilder withReportingStrategy(ReportingStrategy reportingStrategy) {
      this.reportingStrategy = reportingStrategy;
      return this;
    }

    public MigrationJobBuilder withOutputProjectType(ProjectType projectType) {
      this.outputProjectType = projectType;
      return this;
    }

    public MigrationJobBuilder withInputVersion(Version inputVersion) {
      this.inputVersion = inputVersion;
      return this;
    }

    public MigrationJobBuilder withOuputVersion(Version outputVersion) {
      this.outputVersion = outputVersion;
      return this;
    }

    public MigrationJob build() {
      checkState(project != null, "The project must not be null");
      checkState(outputProject != null, "The output project must not be null");
      checkState(outputProjectType != null, "The output project type must not be null");
      checkState(inputVersion != null, "The input version must not be null");
      checkState(outputVersion != null, "The output version must not be null");

      MigrationTaskLocator migrationTaskLocator = new MigrationTaskLocator(inputVersion, outputVersion, outputProjectType);
      migrationTasks = migrationTaskLocator.locate();

      return new MigrationJob(project, outputProject, migrationTasks, reportingStrategy);
    }
  }

}
