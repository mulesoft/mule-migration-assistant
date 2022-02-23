/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.engine;


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
import com.mulesoft.tools.migration.project.model.pom.Parent;
import com.mulesoft.tools.migration.report.html.HTMLReport;
import com.mulesoft.tools.migration.report.html.model.ReportEntryModel;
import com.mulesoft.tools.migration.report.json.JSONReport;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static com.mulesoft.tools.migration.engine.project.MuleProjectFactory.getMuleProject;
import static com.mulesoft.tools.migration.engine.project.structure.BasicProject.getFiles;
import static com.mulesoft.tools.migration.project.ProjectType.*;
import static com.mulesoft.tools.migration.util.version.VersionUtils.MIN_MULE4_VALID_VERSION;
import static com.mulesoft.tools.migration.util.version.VersionUtils.isVersionValid;
import static com.mulesoft.tools.migration.xml.AdditionalNamespacesFactory.getTasksDeclaredNamespaces;

/**
 * It represent a migration job which is composed by one or more {@link AbstractMigrationTask}
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MigrationJob implements Executable {

  private static final String HTML_REPORT_FOLDER = "report";
  private final boolean jsonReportEnabled;
  private transient Logger logger = LoggerFactory.getLogger(this.getClass());

  private final Path project;
  private final Path parentDomainProject;
  private final Path outputProject;
  private final Path reportPath;
  private final List<AbstractMigrationTask> migrationTasks;
  private final String muleVersion;
  private final boolean cancelOnError;
  private String runnerVersion;
  private final Parent projectParentGAV;
  private final String projectGAV;

  private MigrationJob(Path project, Path parentDomainProject, Path outputProject, List<AbstractMigrationTask> migrationTasks,
                       String muleVersion, boolean cancelOnError, Parent projectParentGAV, String projectGAV,
                       boolean jsonReportEnabled) {
    this.migrationTasks = migrationTasks;
    this.muleVersion = muleVersion;
    this.outputProject = outputProject;
    this.project = project;
    this.parentDomainProject = parentDomainProject;
    this.reportPath = outputProject.resolve(HTML_REPORT_FOLDER);
    this.cancelOnError = cancelOnError;
    this.projectParentGAV = projectParentGAV;
    this.projectGAV = projectGAV;
    this.jsonReportEnabled = jsonReportEnabled;
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
    applicationModel =
        generateTargetApplicationModel(outputProject, targetProjectType, sourceProjectBasePath, projectParentGAV, projectGAV);
    try {
      for (AbstractMigrationTask task : migrationTasks) {
        if (task.getApplicableProjectTypes().contains(targetProjectType)) {
          task.setApplicationModel(applicationModel);
          task.setExpressionMigrator(new MelToDwExpressionMigrator(report, applicationModel));
          try {
            task.execute(report);
            persistApplicationModel(applicationModel);
            applicationModel =
                generateTargetApplicationModel(outputProject, targetProjectType, sourceProjectBasePath, projectParentGAV,
                                               projectGAV);
          } catch (MigrationTaskException ex) {
            if (cancelOnError) {
              throw ex;
            } else {
              logger.error("Failed to apply task, rolling back and continuing with the next one.", ex);
            }
          } catch (RuntimeException e) {
            throw new MigrationJobException("Failed to continue executing migration: " + e.getClass().getName() + ": "
                + e.getMessage(), e);
          }
        }
      }
    } finally {
      generateReport(report, applicationModel);
    }
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
        .withProjectPomGAV(projectGAV)
        .withProjectBasePath(muleProject.getBaseFolder())
        .withSupportedNamespaces(getTasksDeclaredNamespaces(migrationTasks));
    if (muleProject.srcTestConfiguration().toFile().exists()) {
      builder.withTestConfigurationFiles(getFiles(muleProject.srcTestConfiguration(), "xml"));
    }
    return builder.build();
  }

  private ApplicationModel generateTargetApplicationModel(Path project, ProjectType type, Path sourceProjectBasePath,
                                                          Parent projectParentGAV, String projectGAV)
      throws Exception {
    ApplicationModelBuilder appModelBuilder = new ApplicationModelBuilder()
        .withMuleVersion(muleVersion)
        .withSupportedNamespaces(getTasksDeclaredNamespaces(migrationTasks))
        .withSourceProjectBasePath(sourceProjectBasePath)
        .withProjectPomParent(projectParentGAV)
        .withProjectPomGAV(projectGAV);

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

  private void generateReport(MigrationReport<ReportEntryModel> report, ApplicationModel applicationModel) throws Exception {
    List<ReportEntryModel> reportEntries = report.getReportEntries();
    for (ReportEntryModel entry : reportEntries) {
      try {
        entry.setElementLocation();
      } catch (Exception ex) {
        throw new MigrationJobException("Failed to generate report.", ex);
      }
    }
    HTMLReport htmlReport = new HTMLReport(report, reportPath.toFile(), this.getRunnerVersion());
    htmlReport.printReport();
    if (jsonReportEnabled) {
      applicationModel.getPomModel().ifPresent(p -> report.addConnectors(p));
      JSONReport jsonReport = new JSONReport(report, reportPath.toFile(), outputProject);
      jsonReport.printReport();
    }
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
    private boolean cancelOnError = false;
    private boolean jsonReportEnabled = false;
    private List<AbstractMigrationTask> migrationTasks = new ArrayList<>();
    private Parent projectParentGAV = null;
    private String projectGAV;

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

    public MigrationJobBuilder withCancelOnError(boolean cancelOnError) {
      this.cancelOnError = cancelOnError;
      return this;
    }

    public MigrationJobBuilder withProjectParentGAV(Parent projectParentGAV) {
      this.projectParentGAV = projectParentGAV;
      return this;
    }

    public MigrationJobBuilder withProjectGAV(String projectGAV) {
      this.projectGAV = projectGAV;
      return this;
    }

    public MigrationJobBuilder withJsonReport(Boolean jsonReportEnabled) {
      this.jsonReportEnabled = jsonReportEnabled;
      return this;
    }

    public MigrationJob build() throws Exception {
      checkState(project != null, "The project must not be null");
      if (!project.toFile().exists()) {
        throw new MigrationJobException("`projectBasePath` " + project.toString() + " does not exist");
      }
      if (!project.toFile().isDirectory()) {
        throw new MigrationJobException("`projectBasePath` " + project.toString() + " is not a directory");
      }

      if (parentDomainProject != null) {
        if (!parentDomainProject.toFile().exists()) {
          throw new MigrationJobException("`parentDomainBasePath` " + project.toString() + " does not exist");
        }
        if (!parentDomainProject.toFile().isDirectory()) {
          throw new MigrationJobException("`parentDomainBasePath` " + project.toString() + " is not a directory");
        }
      }

      checkState(inputVersion != null, "The input version must not be null");

      checkState(outputVersion != null, "The output version must not be null");
      if (!isVersionValid(outputVersion, MIN_MULE4_VALID_VERSION)) {
        throw new MigrationJobException("Output Version " + outputVersion
            + " does not comply with semantic versioning specification");
      }

      checkState(outputProject != null, "The output project must not be null");
      if (outputProject.toFile().exists()) {
        throw new MigrationJobException("Destination folder already exist.");
      }

      MigrationTaskLocator migrationTaskLocator = new MigrationTaskLocator(inputVersion, outputVersion);
      migrationTasks = migrationTaskLocator.locate();

      return new MigrationJob(project, parentDomainProject, outputProject, migrationTasks, outputVersion.toString(),
                              this.cancelOnError, this.projectParentGAV, this.projectGAV, this.jsonReportEnabled);
    }
  }

}
