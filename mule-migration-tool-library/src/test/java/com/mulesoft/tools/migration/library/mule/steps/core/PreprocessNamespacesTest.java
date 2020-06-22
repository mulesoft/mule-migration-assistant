/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

import org.jdom2.Document;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.Iterables;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class PreprocessNamespacesTest {

  private static final String FILE_SAMPLE_XML = "unsupported-namespace.xml";
  private static final Path FILE_EXAMPLES_PATH = Paths.get("mule/examples/core/namespaces");
  private static final Path FILE_SAMPLE_PATH = FILE_EXAMPLES_PATH.resolve(FILE_SAMPLE_XML);

  private PreprocessNamespaces preprocessNamespaces;
  private ApplicationModel applicationModel;
  private Document doc;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(FILE_SAMPLE_PATH.toString()).toURI().getPath());
    Map<Path, Document> appDocs = new HashMap<>();
    appDocs.put(FILE_SAMPLE_PATH, doc);
    applicationModel = mock(ApplicationModel.class);
    when(applicationModel.getApplicationDocuments())
        .thenAnswer(invocation -> appDocs);
    preprocessNamespaces = new PreprocessNamespaces();
    applicationModel.setSupportedNamespaces(newArrayList());
  }

  @Test
  public void execute() throws Exception {
    preprocessNamespaces.execute(applicationModel, report.getReport());
    Document document = Iterables.get(applicationModel.getApplicationDocuments().values(), 0);
    assertThat("The namespace was removed.", document.getRootElement().getAdditionalNamespaces().size(), is(3));
  }
}
