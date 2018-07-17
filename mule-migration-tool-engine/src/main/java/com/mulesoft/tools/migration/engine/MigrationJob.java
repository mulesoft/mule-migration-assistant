/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine;


import com.mulesoft.tools.migration.Executable;
import com.mulesoft.tools.migration.engine.exception.MigrationJobException;
import com.mulesoft.tools.migration.engine.project.structure.ApplicationPersister;
import com.mulesoft.tools.migration.engine.project.structure.mule.MuleProject;
import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourApplication;
import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourDomain;
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

import static com.google.common.base.Preconditions.checkState;
import static com.mulesoft.tools.migration.engine.project.MuleProjectFactory.getMuleProject;
import static com.mulesoft.tools.migration.engine.project.structure.BasicProject.getFiles;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;
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

  private Path project;
  private Path outputProject;
  private Path reportPath;
  private List<AbstractMigrationTask> migrationTasks;
  private String muleVersion;

  private MigrationJob(Path project, Path outputProject, List<AbstractMigrationTask> migrationTasks, String muleVersion) {
    this.migrationTasks = migrationTasks;
    this.muleVersion = muleVersion;
    this.outputProject = outputProject;
    this.project = project;
    this.reportPath = outputProject.resolve(HTML_REPORT_FOLDER);
  }

  @Override
  public void execute(MigrationReport report) throws Exception {
    ApplicationModel applicationModel = generateApplicationModel(project);
    persistApplicationModel(applicationModel);
    applicationModel = generateApplicationModel(outputProject, MULE_FOUR_APPLICATION);
    for (AbstractMigrationTask task : migrationTasks) {
      task.setApplicationModel(applicationModel);
      task.setExpressionMigrator(new MelToDwExpressionMigrator(report, applicationModel));
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
    generateReport(report);
  }

  private void persistApplicationModel(ApplicationModel applicationModel) throws Exception {
    ApplicationPersister persister = new ApplicationPersister(applicationModel, outputProject);
    persister.persist();
  }

  private ApplicationModel generateApplicationModel(Path project) throws Exception {
    MuleProject muleProject = getMuleProject(project);
    ApplicationModelBuilder builder = new ApplicationModelBuilder()
        .withConfigurationFiles(getFiles(muleProject.srcMainConfiguration(), "xml"))
        .withMuleVersion(muleVersion)
        .withPom(muleProject.pom())
        .withProjectBasePath(muleProject.getBaseFolder())
        .withSupportedNamespaces(getTasksDeclaredNamespaces(migrationTasks));
    if (muleProject.srcTestConfiguration().toFile().exists()) {
      builder.withTestConfigurationFiles(getFiles(muleProject.srcTestConfiguration(), "xml"));
    }
    return builder.build();
  }

  private ApplicationModel generateApplicationModel(Path project, ProjectType type) throws Exception {
    if (type.equals(MULE_FOUR_APPLICATION)) {
      MuleFourApplication application = new MuleFourApplication(project);
      return new ApplicationModelBuilder()
          .withConfigurationFiles(getFiles(application.srcMainConfiguration(), "xml"))
          .withTestConfigurationFiles(getFiles(application.srcTestConfiguration(), "xml"))
          .withMuleArtifactJson(application.muleArtifactJson())
          .withMuleVersion(muleVersion)
          .withSupportedNamespaces(getTasksDeclaredNamespaces(migrationTasks))
          .withProjectBasePath(application.getBaseFolder())
          .withPom(application.pom()).build();
    } else {
      MuleFourDomain domain = new MuleFourDomain(project);
      return new ApplicationModelBuilder()
          .withConfigurationFiles(getFiles(domain.srcMainConfiguration(), "xml"))
          .withMuleVersion(muleVersion)
          .withProjectBasePath(domain.getBaseFolder())
          .withPom(domain.pom()).build();
    }
  }

  private void generateReport(MigrationReport report) throws Exception {
    List<ReportEntryModel> reportEntries = report.getReportEntries();
    for (ReportEntryModel entry : reportEntries) {
      try {
        entry.setElementLocation();
      } catch (Exception ex) {
        throw new MigrationJobException("Failed to generate report.", ex.getCause());
      }
    }
    HTMLReport htmlReport = new HTMLReport(report.getReportEntries(), reportPath.toFile());
    htmlReport.printReport();
  }

  public Path getReportPath() {
    return this.reportPath;
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
    private String inputVersion;
    private String outputVersion;
    private List<AbstractMigrationTask> migrationTasks = new ArrayList<>();

    public MigrationJobBuilder withProject(Path project) {
      this.project = project;
      return this;
    }

    public MigrationJobBuilder withOutputProject(Path outputProject) {
      this.outputProject = outputProject;
      return this;
    }

    public MigrationJobBuilder withOutputProjectType(ProjectType projectType) {
      this.outputProjectType = projectType;
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
      checkState(outputProjectType != null, "The output project type must not be null");
      checkState(inputVersion != null, "The input version must not be null");

      if (!isVersionValid(outputVersion, MIN_MULE4_VALID_VERSION)) {
        throw new MigrationJobException("Output Version " + outputVersion
            + " does not comply with semantic versioning specification");
      }

      if (outputProject.toFile().exists()) {
        throw new MigrationJobException("Destination folder already exist.");
      }

      MigrationTaskLocator migrationTaskLocator = new MigrationTaskLocator(inputVersion, outputVersion, outputProjectType);
      migrationTasks = migrationTaskLocator.locate();

      return new MigrationJob(project, outputProject, migrationTasks, outputVersion.toString());
    }
  }

}
