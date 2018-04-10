/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.google.common.collect.Iterables;
import com.mulesoft.tools.migration.engine.exception.MigrationStepException;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import org.jdom2.Document;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.mulesoft.tools.migration.utils.ApplicationModelUtils.generateAppModel;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RemoveJsonTransformerNamespaceTest {

  private static final String FILE_SAMPLE_XML = "jsonTransformer.xml";
  private static final Path FILE_EXAMPLES_PATH = Paths.get("mule/examples/core");
  private static final Path FILE_SAMPLE_PATH = FILE_EXAMPLES_PATH.resolve(FILE_SAMPLE_XML);
  private static final String PROJECT_NAME = "test-app";

  private ApplicationModel applicationModel;
  private List<URL> applicationDocuments = new ArrayList<>();
  private Path projectPath;
  private RemoveJsonTransformerNamespace removeJsonTransformerNamespace;
  private URL documentPath;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void setUp() throws Exception {
    removeJsonTransformerNamespace = new RemoveJsonTransformerNamespace();
    projectPath = temporaryFolder.newFolder(PROJECT_NAME).toPath();
    documentPath = this.getClass().getClassLoader().getResource(FILE_SAMPLE_PATH.toString());
    applicationDocuments.add(documentPath);
    applicationModel = generateAppModel(applicationDocuments, projectPath);
  }

  @Test(expected = MigrationStepException.class)
  public void executeWithNullElement() throws Exception {
    removeJsonTransformerNamespace.execute(null);
  }

  @Test
  public void execute() throws Exception {
    removeJsonTransformerNamespace.execute(applicationModel);
    Document document = Iterables.get(applicationModel.getApplicationDocuments().values(), 0);
    assertThat("The namespace wasn't removed.", document.getRootElement().getAdditionalNamespaces().size(), is(2));
  }
}
