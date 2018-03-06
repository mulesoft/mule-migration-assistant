/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine;


import static com.google.common.base.Preconditions.checkState;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map.Entry;

import com.mulesoft.tools.migration.engine.task.DefaultMigrationTask;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.mulesoft.tools.migration.engine.exception.MigrationJobException;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.ApplicationModel.ApplicationModelBuilder;
import com.mulesoft.tools.migration.project.structure.BasicProject;
import com.mulesoft.tools.migration.project.structure.mule.three.MuleApplicationProject;
import com.mulesoft.tools.migration.report.ReportingStrategy;
import com.mulesoft.tools.migration.report.console.ConsoleReportStrategy;
import com.mulesoft.tools.migration.report.html.HTMLReportStrategy;

/**
 * It represent a migration job which is composed by one or more {@link DefaultMigrationTask}
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MigrationJob implements Executable {

  private Boolean onErrorStop;
  private ReportingStrategy reportingStrategy;

  private BasicProject project;
  private BasicProject outputProject;

  private List<DefaultMigrationTask> migrationTasks;

  private MigrationJob(BasicProject project, BasicProject outputProject, List<DefaultMigrationTask> migrationTasks,
                       Boolean onErrorStop, ReportingStrategy reportingStrategy) {
    this.project = project;
    this.outputProject = outputProject;

    this.migrationTasks = migrationTasks;

    this.onErrorStop = onErrorStop;
    this.reportingStrategy = reportingStrategy;
  }

  public void execute() throws Exception {
    // TODO this casting should be smarter
    ApplicationModel applicationModel = new ApplicationModelBuilder((MuleApplicationProject) project).build();

    for (DefaultMigrationTask task : migrationTasks) {
      task.setApplicationModel(applicationModel);

      try {
        task.execute();
        // TODO we should review this
        persistApplicationModel(applicationModel);
      } catch (Exception e) {
        throw new MigrationJobException("Failed to execute task: " + task.getDescription() + ". ", e);
      }
    }
    generateReport();
  }

  private void persistApplicationModel(ApplicationModel applicationModel) throws IOException {
    for (Entry<Path, Document> entry : applicationModel.getApplicationDocuments().entrySet()) {
      Path originalFilePath = entry.getKey();
      Document document = entry.getValue();

      // TODO this is just wrong
      String targetFilePath = outputProject.getBaseFolder().resolve(originalFilePath.getFileName()).toString();
      XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
      xmlOutputter.output(document, new FileOutputStream(targetFilePath));
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

    private MuleApplicationProject project;
    private MuleApplicationProject outputProject;
    private List<DefaultMigrationTask> migrationTasks;

    private Boolean onErrorStop;
    private ReportingStrategy reportingStrategy = new ConsoleReportStrategy();

    public MigrationJobBuilder withProject(MuleApplicationProject project) {
      this.project = project;
      return this;
    }

    public MigrationJobBuilder withOutputProject(MuleApplicationProject outputProject) {
      this.outputProject = outputProject;
      return this;
    }

    public MigrationJobBuilder withMigrationTasks(List<DefaultMigrationTask> migrationTasks) {
      this.migrationTasks = migrationTasks;
      return this;
    }

    public MigrationJobBuilder withOnErrorStop(Boolean onErrorStop) {
      this.onErrorStop = onErrorStop;
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

      return new MigrationJob(project, outputProject, migrationTasks, onErrorStop, reportingStrategy);
    }
  }

}
