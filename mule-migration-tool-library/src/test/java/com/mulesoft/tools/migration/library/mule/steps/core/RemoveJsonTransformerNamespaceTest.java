/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.utils.ApplicationModelUtils.generateAppModel;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.Iterables;
import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class RemoveJsonTransformerNamespaceTest {

  private static final String FILE_SAMPLE_XML = "jsonTransformer.xml";
  private static final Path FILE_EXAMPLES_PATH = Paths.get("mule/examples/core");
  private static final Path FILE_SAMPLE_PATH = FILE_EXAMPLES_PATH.resolve(FILE_SAMPLE_XML);
  private static final String APP_NAME = "transformers";

  @Rule
  public ReportVerification report = new ReportVerification();

  private ApplicationModel applicationModel;
  private RemoveJsonTransformerNamespace removeJsonTransformerNamespace;
  private Path appPath;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void setUp() throws Exception {
    buildProject();
    applicationModel = generateAppModel(appPath);
    applicationModel.setSupportedNamespaces(newArrayList());
    removeJsonTransformerNamespace = new RemoveJsonTransformerNamespace();
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
    removeJsonTransformerNamespace.execute(null, report.getReport());
  }

  @Test
  public void execute() throws Exception {
    removeJsonTransformerNamespace.execute(applicationModel, report.getReport());
    Document document = Iterables.get(applicationModel.getApplicationDocuments().values(), 0);
    assertThat("The namespace wasn't removed.", document.getRootElement().getAdditionalNamespaces().size(), is(2));
  }
}
