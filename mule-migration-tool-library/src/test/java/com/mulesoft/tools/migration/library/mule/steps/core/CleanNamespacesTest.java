/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Iterables;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.jdom2.Document;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class CleanNamespacesTest {

  private static final String FILE_SAMPLE_XML = "unused-namespace.xml";
  private static final Path FILE_EXAMPLES_PATH = Paths.get("mule/examples/core/namespaces");
  private static final Path FILE_SAMPLE_PATH = FILE_EXAMPLES_PATH.resolve(FILE_SAMPLE_XML);

  @Rule
  public ReportVerification report = new ReportVerification();

  private CleanNamespaces cleanNamespaces;
  private ApplicationModel applicationModel;
  private Document doc;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(FILE_SAMPLE_PATH.toString()).toURI().getPath());
    Map<Path, Document> appDocs = new HashMap<>();
    appDocs.put(FILE_SAMPLE_PATH, doc);
    applicationModel = mock(ApplicationModel.class);
    when(applicationModel.getApplicationDocuments())
        .thenAnswer(invocation -> appDocs);
    cleanNamespaces = new CleanNamespaces();
  }


  @Test
  public void execute() throws Exception {
    Document document = Iterables.get(applicationModel.getApplicationDocuments().values(), 0);
    assertThat("The namespace wasn't removed.", document.getRootElement().getAdditionalNamespaces().size(), is(5));
    assertThat("The schemas weren't removed.", document.getRootElement()
        .getAttribute("schemaLocation", document.getRootElement().getNamespace("xsi")).getValue().split("\\s+").length, is(8));
    cleanNamespaces.execute(applicationModel, report.getReport());
    document = Iterables.get(applicationModel.getApplicationDocuments().values(), 0);
    assertThat("The namespace wasn't removed.", document.getRootElement().getAdditionalNamespaces().size(), is(2));
    assertThat("The schemas weren't removed.", document.getRootElement()
        .getAttribute("schemaLocation", document.getRootElement().getNamespace("xsi")).getValue().split("\\s+").length, is(2));
  }
}
