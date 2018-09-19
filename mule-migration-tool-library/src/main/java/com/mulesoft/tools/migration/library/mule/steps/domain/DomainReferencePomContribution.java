/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.domain;

import static java.io.File.separator;
import static java.lang.String.format;

import com.mulesoft.tools.migration.exception.MigrationAbortException;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.Dependency.DependencyBuilder;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Adds the Domain dependency
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class DomainReferencePomContribution implements PomContribution {

  @Override
  public String getDescription() {
    return "Add Domain dependency.";
  }

  private ApplicationModel applicationModel;

  @Override
  public void execute(PomModel object, MigrationReport report) throws RuntimeException {
    Properties properties = new Properties();
    File file =
        getApplicationModel().getSourceProjectBasePath()
            .resolve("src" + separator + "main" + separator + "app" + separator + "mule-deploy.properties").toFile();
    if (!file.exists()) {
      return;
    }

    try (FileInputStream inStream = new FileInputStream(file)) {
      properties.load(inStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    if (!properties.containsKey("domain")) {
      return;
    }

    String domain = properties.getProperty("domain");
    if ("default".equals(domain)) {
      return;
    }

    if (getApplicationModel().getDomainDocuments() == null || getApplicationModel().getDomainDocuments().isEmpty()) {
      throw new MigrationAbortException(format("The application to migrate references a domain '%s'. "
          + "Call the migrator with the 'parentDomainBasePath' parameter indicating the Mule 3 source for that domain.", domain));
    }

    object.addDependency(new DependencyBuilder()
        .withGroupId("org.mule.migrated")
        .withArtifactId(domain)
        .withVersion("1.0.0-M4-SNAPSHOT")
        .withClassifier("mule-domain")
        .withScope("provided")
        .build());
  }

  @Override
  public ApplicationModel getApplicationModel() {
    return applicationModel;
  }

  @Override
  public void setApplicationModel(ApplicationModel appModel) {
    this.applicationModel = appModel;
  }
}
