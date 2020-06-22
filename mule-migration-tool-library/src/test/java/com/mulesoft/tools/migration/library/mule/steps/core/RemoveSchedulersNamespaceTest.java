/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.utils.ApplicationModelUtils.generateAppModel;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.Iterables;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RemoveSchedulersNamespaceTest {

  private static final String FILE_SAMPLE_XML = "poll-03-original.xml";
  private static final Path FILE_EXAMPLES_PATH = Paths.get("mule/apps/poll");
  private static final Path FILE_SAMPLE_PATH = FILE_EXAMPLES_PATH.resolve(FILE_SAMPLE_XML);
  private static final String APP_NAME = "schedulers";

  private ApplicationModel applicationModel;
  private RemoveSchedulersNamespace removeSchedulersNamespace;
  private Path appPath;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  @Before
  public void setUp() throws Exception {
    buildProject();
    applicationModel = generateAppModel(appPath);
    removeSchedulersNamespace = new RemoveSchedulersNamespace();
  }

  private void buildProject() throws IOException {
    appPath = temporaryFolder.newFolder(APP_NAME).toPath();
    File app = appPath.resolve("src").resolve("main").resolve("app").toFile();
    app.mkdirs();

    URL sample = this.getClass().getClassLoader().getResource(FILE_SAMPLE_PATH.toString());
    FileUtils.copyURLToFile(sample, new File(app, FILE_SAMPLE_PATH.getFileName().toString()));
  }

  @Test(expected = MigrationStepException.class)
  public void executeWithNullElement() throws Exception {
    removeSchedulersNamespace.execute(null, report.getReport());
  }

  @Test
  public void execute() throws Exception {
    removeSchedulersNamespace.execute(applicationModel, report.getReport());
    Document document = Iterables.get(applicationModel.getApplicationDocuments().values(), 0);
    assertThat("The namespace wasn't removed.", document.getRootElement().getAdditionalNamespaces().size(), is(1));
  }
}
