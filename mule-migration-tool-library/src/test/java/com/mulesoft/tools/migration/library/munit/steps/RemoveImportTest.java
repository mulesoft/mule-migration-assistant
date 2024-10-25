/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.munit.steps;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.mulesoft.tools.migration.library.mule.steps.core.SpringImport;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RemoveImportTest {

  private static final String MUNIT_SAMPLE_XML = "munit-processors.xml";
  private static final Path MUNIT_EXAMPLES_PATH = Paths.get("munit/examples");
  private static final Path MUNIT_SAMPLE_PATH = MUNIT_EXAMPLES_PATH.resolve(MUNIT_SAMPLE_XML);
  private static final String MUNIT_PATH =
      "src" + File.separator + "test" + File.separator + "munit" + File.separator + MUNIT_SAMPLE_XML;

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private Document doc;
  private ApplicationModel appModel;
  private SpringImport springImport;
  private RemoveImport removeImport;
  private Element node;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(MUNIT_SAMPLE_PATH.toString()).toURI().getPath());
    doc.setBaseURI(MUNIT_PATH);

    appModel = mockApplicationModel(doc, temp);

    springImport = new SpringImport();
    springImport.setApplicationModel(appModel);
    removeImport = new RemoveImport();
  }

  @Test
  public void execute() throws Exception {
    for (Element node : getElementsFromDocument(doc, springImport.getAppliedTo().getExpression())) {
      springImport.execute(node, report.getReport());
    }

    assertThat("There is no spring section defined on doc.", doc.getRootElement().getChildren().size(), is(10));
    for (Element node : getElementsFromDocument(doc, removeImport.getAppliedTo().getExpression())) {
      removeImport.execute(node, report.getReport());
      assertThat("The spring section wasn't removed.", node.getParent(), nullValue());
    }

    assertThat("The spring section wasn't removed.", doc.getRootElement().getChildren().size(), is(10));
  }

}
