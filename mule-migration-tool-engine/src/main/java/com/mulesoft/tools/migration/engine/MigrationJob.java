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
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_DOMAIN;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_POLICY;
import static com.mulesoft.tools.migration.util.version.VersionUtils.MIN_MULE4_VALID_VERSION;
import static com.mulesoft.tools.migration.util.version.VersionUtils.isVersionValid;
import static com.mulesoft.tools.migration.xml.AdditionalNamespacesFactory.getTasksDeclaredNamespaces;

import com.mulesoft.tools.migration.Executable;
import com.mulesoft.tools.migration.engine.exception.MigrationJobException;
import com.mulesoft.tools.migration.engine.project.ProjectTypeFactory;
import com.mulesoft.tools.migration.engine.project.structure.ApplicationPersister;
import com.mulesoft.tools.migration.engine.project.structure.mule.MuleProject;
import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourApplication;
import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourDomain;
import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourPolicy;
import com.mulesoft.tools.migration.exception.MigrationTaskException;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.ApplicationModel.ApplicationModelBuilder;
import com.mulesoft.tools.migration.report.html.HTMLReport;
import com.mulesoft.tools.migration.report.html.model.ReportEntryModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

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

  private static final String HTML_REPORT_FOLDER = "report";
  private transient Logger logger = LoggerFactory.getLogger(this.getClass());

  private final Path project;
  private final Path parentDomainProject;
  private final Path outputProject;
  private final Path reportPath;
  private final List<AbstractMigrationTask> migrationTasks;
  private final String muleVersion;
  private String runnerVersion;

  private MigrationJob(Path project, Path parentDomainProject, Path outputProject, List<AbstractMigrationTask> migrationTasks,
                       String muleVersion) {
    this.migrationTasks = migrationTasks;
    this.muleVersion = muleVersion;
    this.outputProject = outputProject;
    this.project = project;
    this.parentDomainProject = parentDomainProject;
    this.reportPath = outputProject.resolve(HTML_REPORT_FOLDER);
    this.runnerVersion = this.getClass().getPackage().getImplementationVersion();
    if (this.runnerVersion == null) {
      this.runnerVersion = "n/a";
    }
  }

  @Override
  public void execute(MigrationReport report) throws Exception {
    ApplicationModel applicationModel = generateSourceApplicationModel(project);

    report.initialize(applicationModel.getProjectType(), project.getFileName().toString());

    Path sourceProjectBasePath = applicationModel.getProjectBasePath();
    persistApplicationModel(applicationModel);
    ProjectType targetProjectType = applicationModel.getProjectType().getTargetType();
    applicationModel = generateTargetApplicationModel(outputProject, targetProjectType, sourceProjectBasePath);
    for (AbstractMigrationTask task : migrationTasks) {
      if (task.getApplicableProjectTypes().contains(targetProjectType)) {
        task.setApplicationModel(applicationModel);
        task.setExpressionMigrator(new MelToDwExpressionMigrator(report, applicationModel));
        try {
          task.execute(report);
          persistApplicationModel(applicationModel);
          applicationModel = generateTargetApplicationModel(outputProject, targetProjectType, sourceProjectBasePath);
        } catch (MigrationTaskException ex) {
          logger.error("Failed to apply task, rolling back and continuing with the next one.", ex);
        } catch (Exception e) {
          throw new MigrationJobException("Failed to continue executing migration: " + e.getClass().getName() + ": "
              + e.getMessage(), e);
        }
      }
    }
    generateReport(report);
  }

  private void persistApplicationModel(ApplicationModel applicationModel) throws Exception {
    ApplicationPersister persister = new ApplicationPersister(applicationModel, outputProject);
    persister.persist();
  }

  private ApplicationModel generateSourceApplicationModel(Path project) throws Exception {
    ProjectTypeFactory projectFactory = new ProjectTypeFactory();
    ProjectType type = projectFactory.getProjectType(project);

    MuleProject muleProject = getMuleProject(project, type);
    ApplicationModelBuilder builder = new ApplicationModelBuilder()
        .withConfigurationFiles(getFiles(muleProject.srcMainConfiguration(), "xml"))
        .withProjectType(type)
        .withMuleVersion(muleVersion)
        .withPom(muleProject.pom())
        .withProjectBasePath(muleProject.getBaseFolder())
        .withSupportedNamespaces(getTasksDeclaredNamespaces(migrationTasks));
    if (muleProject.srcTestConfiguration().toFile().exists()) {
      builder.withTestConfigurationFiles(getFiles(muleProject.srcTestConfiguration(), "xml"));
    }
    return builder.build();
  }

  private ApplicationModel generateTargetApplicationModel(Path project, ProjectType type, Path sourceProjectBasePath)
      throws Exception {
    ApplicationModelBuilder appModelBuilder = new ApplicationModelBuilder()
        .withMuleVersion(muleVersion)
        .withSupportedNamespaces(getTasksDeclaredNamespaces(migrationTasks))
        .withSourceProjectBasePath(sourceProjectBasePath);

    if (type.equals(MULE_FOUR_APPLICATION)) {
      MuleFourApplication application = new MuleFourApplication(project);
      return appModelBuilder
          .withConfigurationFiles(getFiles(application.srcMainConfiguration(), "xml"))
          .withTestConfigurationFiles(getFiles(application.srcTestConfiguration(), "xml"))
          .withMuleArtifactJson(application.muleArtifactJson())
          .withProjectBasePath(application.getBaseFolder())
          .withParentDomainBasePath(parentDomainProject)
          .withPom(application.pom()).build();
    } else if (type.equals(MULE_FOUR_DOMAIN)) {
      MuleFourDomain domain = new MuleFourDomain(project);
      return appModelBuilder
          .withConfigurationFiles(getFiles(domain.srcMainConfiguration(), "xml"))
          .withMuleArtifactJson(domain.muleArtifactJson())
          .withProjectBasePath(domain.getBaseFolder())
          .withPom(domain.pom()).build();
    } else if (type.equals(MULE_FOUR_POLICY)) {
      MuleFourPolicy policy = new MuleFourPolicy(project);
      return appModelBuilder
          .withConfigurationFiles(getFiles(policy.srcMainConfiguration(), "xml"))
          .withMuleArtifactJson(policy.muleArtifactJson())
          .withProjectBasePath(policy.getBaseFolder())
          .withPom(policy.pom()).build();
    } else {
      throw new MigrationJobException("Undetermined project type");
    }
  }

  private void generateReport(MigrationReport report) throws Exception {
    List<ReportEntryModel> reportEntries = report.getReportEntries();
    for (ReportEntryModel entry : reportEntries) {
      try {
        entry.setElementLocation();
      } catch (Exception ex) {
        throw new MigrationJobException("Failed to generate report.", ex);
      }
    }
    HTMLReport htmlReport = new HTMLReport(report.getReportEntries(), reportPath.toFile(), this.getRunnerVersion());
    htmlReport.printReport();
  }

  public Path getReportPath() {
    return this.reportPath;
  }

  public String getRunnerVersion() {
    return this.runnerVersion;
  }

  /**
   * It represent the builder to obtain a {@link MigrationJob}
   *
   * @author Mulesoft Inc.
   * @since 1.0.0
   */
  public static class MigrationJobBuilder {

    private Path project;
    private Path parentDomainProject;
    private Path outputProject;
    private String inputVersion;
    private String outputVersion;
    private List<AbstractMigrationTask> migrationTasks = new ArrayList<>();

    public MigrationJobBuilder withProject(Path project) {
      this.project = project;
      return this;
    }

    public MigrationJobBuilder withParentDomainProject(Path parentDomainProject) {
      this.parentDomainProject = parentDomainProject;
      return this;
    }

    public MigrationJobBuilder withOutputProject(Path outputProject) {
      this.outputProject = outputProject;
      return this;
    }

    public MigrationJobBuilder withInputVersion(String inputVersion) {
      this.inputVersion = inputVersion;
      return this;
    }

    public MigrationJobBuilder withOuputVersion(String outputVersion) {
      this.outputVersion = outputVersion;
      return this;
    }

    public MigrationJob build() throws Exception {
      checkState(project != null, "The project must not be null");
      checkState(outputProject != null, "The output project must not be null");
      checkState(inputVersion != null, "The input version must not be null");

      if (!isVersionValid(outputVersion, MIN_MULE4_VALID_VERSION)) {
        throw new MigrationJobException("Output Version " + outputVersion
            + " does not comply with semantic versioning specification");
      }

      if (outputProject.toFile().exists()) {
        throw new MigrationJobException("Destination folder already exist.");
      }

      MigrationTaskLocator migrationTaskLocator = new MigrationTaskLocator(inputVersion, outputVersion);
      migrationTasks = migrationTaskLocator.locate();

      return new MigrationJob(project, parentDomainProject, outputProject, migrationTasks, outputVersion.toString());
    }
  }

}
