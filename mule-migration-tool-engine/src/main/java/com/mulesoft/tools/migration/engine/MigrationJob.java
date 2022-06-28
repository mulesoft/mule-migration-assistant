/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.engine;


import com.google.common.collect.Lists;
import com.mulesoft.tools.migration.Executable;
import com.mulesoft.tools.migration.engine.exception.MigrationJobException;
import com.mulesoft.tools.migration.engine.project.ProjectTypeFactory;
import com.mulesoft.tools.migration.engine.project.structure.ApplicationPersister;
import com.mulesoft.tools.migration.engine.project.structure.mule.MuleProject;
import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourApplication;
import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourDomain;
import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourPolicy;
import com.mulesoft.tools.migration.exception.MigrationTaskException;
import com.mulesoft.tools.migration.library.applicationgraph.ApplicationGraphCreator;
import com.mulesoft.tools.migration.project.model.applicationgraph.GraphRenderer;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.ApplicationModel.ApplicationModelBuilder;
import com.mulesoft.tools.migration.project.model.applicationgraph.ApplicationGraph;
import com.mulesoft.tools.migration.project.model.pom.Parent;
import com.mulesoft.tools.migration.report.html.HTMLReport;
import com.mulesoft.tools.migration.report.html.model.ReportEntryModel;
import com.mulesoft.tools.migration.report.json.JSONReport;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static com.mulesoft.tools.migration.engine.project.MuleProjectFactory.getMuleProject;
import static com.mulesoft.tools.migration.engine.project.structure.BasicProject.getFiles;
import static com.mulesoft.tools.migration.library.mule.steps.spring.AbstractSpringMigratorStep.SPRING_FOLDER;
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
  private final boolean jsonReportEnabled;
  private final boolean renderGraph;
  private final ApplicationGraphCreator applicationGraphCreator;

  private MigrationJob(Path project, Path parentDomainProject, Path outputProject, List<AbstractMigrationTask> migrationTasks,
                       String muleVersion, boolean cancelOnError, Parent projectParentGAV, String projectGAV,
                       boolean jsonReportEnabled, boolean noCompatibility, boolean renderGraph) {
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
    this.applicationGraphCreator = noCompatibility ? new ApplicationGraphCreator() : null;
    this.renderGraph = noCompatibility ? renderGraph : false;
  }

  @Override
  public void execute(MigrationReport report) throws Exception {
    ApplicationModel applicationModel = generateSourceApplicationModel(project);

    report.initialize(applicationModel.getProjectType(), project.getFileName().toString());

    Path sourceProjectBasePath = applicationModel.getProjectBasePath();
    persistApplicationModel(applicationModel);
    ProjectType targetProjectType = applicationModel.getProjectType().getTargetType();

    ApplicationGraph applicationGraph = null;
    applicationModel =
        generateTargetApplicationModel(outputProject, targetProjectType, sourceProjectBasePath, projectParentGAV, projectGAV,
                                       true);

    if (applicationGraphCreator != null) {
      applicationGraphCreator.setExpressionMigrator(new MelToDwExpressionMigrator(report, applicationModel));
      applicationGraph = applicationGraphCreator.create(applicationModel, report);
      applicationModel.setApplicationGraph(applicationGraph);
      if (renderGraph) {
        GraphRenderer.render(applicationGraph, project.getFileName().toString());
      }
    }

    try {
      MigrationTaskException firstMigrationTaskException = null;
      for (AbstractMigrationTask task : migrationTasks) {
        if (task.getApplicableProjectTypes().contains(targetProjectType)) {
          task.setApplicationModel(applicationModel);
          task.setExpressionMigrator(new MelToDwExpressionMigrator(report, applicationModel));
          try {
            task.execute(report);
            persistApplicationModel(applicationModel);

            applicationModel =
                generateTargetApplicationModel(outputProject, targetProjectType, sourceProjectBasePath, projectParentGAV,
                                               projectGAV, applicationGraph, false);
          } catch (MigrationTaskException ex) {
            if (cancelOnError) {
              throw ex;
            } else {
              if (firstMigrationTaskException == null) {
                firstMigrationTaskException = ex;
              }
              logger.error("Failed to apply task, rolling back and continuing with the next one.", ex);
            }
          } catch (RuntimeException e) {
            throw new MigrationJobException("Failed to continue executing migration: " + e.getClass().getName() + ": "
                + e.getMessage(), e);
          }
        }
      }
      if (firstMigrationTaskException != null) {
        throw firstMigrationTaskException;
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
                                                          Parent projectParentGAV, String projectGAV, boolean generateElementIds)
      throws Exception {
    return generateTargetApplicationModel(project, type, sourceProjectBasePath, projectParentGAV, projectGAV, null,
                                          generateElementIds);
  }

  private ApplicationModel generateTargetApplicationModel(Path project, ProjectType type, Path sourceProjectBasePath,
                                                          Parent projectParentGAV, String projectGAV, ApplicationGraph graph,
                                                          boolean generateElementIds)
      throws Exception {
    ApplicationModelBuilder appModelBuilder = new ApplicationModelBuilder()
        .withMuleVersion(muleVersion)
        .withSupportedNamespaces(getTasksDeclaredNamespaces(migrationTasks))
        .withSourceProjectBasePath(sourceProjectBasePath)
        .withProjectPomParent(projectParentGAV)
        .withProjectPomGAV(projectGAV)
        .withGenerateElementIds(generateElementIds)
        .withApplicationGraph(graph);

    if (type.equals(MULE_FOUR_APPLICATION)) {
      MuleFourApplication application = new MuleFourApplication(project);
      List<Path> configFiles = getFiles(application.srcMainConfiguration(), "xml");
      Path springPath = application.getBaseFolder().resolve(SPRING_FOLDER);
      if (Files.exists(springPath)) {
        List<Path> springConfigFiles = getFiles(springPath, "xml");
        configFiles.addAll(springConfigFiles);
      }
      return appModelBuilder
          .withConfigurationFiles(configFiles)
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
    private boolean noCompatibility = false;
    private List<AbstractMigrationTask> migrationTasks = new ArrayList<>();
    private Parent projectParentGAV = null;
    private String projectGAV;
    private boolean renderGraph;

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

    public MigrationJobBuilder withNoCompatibility(boolean noCompatibility) {
      this.noCompatibility = noCompatibility;
      return this;
    }

    public MigrationJobBuilder withRenderGraph(boolean renderGraph) {
      this.renderGraph = renderGraph;
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

      MigrationTaskLocator migrationTaskLocator = new MigrationTaskLocator(inputVersion, outputVersion, this.noCompatibility);
      migrationTasks = migrationTaskLocator.locate();

      return new MigrationJob(project, parentDomainProject, outputProject, migrationTasks, outputVersion,
                              this.cancelOnError, this.projectParentGAV, this.projectGAV, this.jsonReportEnabled,
                              this.noCompatibility, this.renderGraph);
    }


  }

}
