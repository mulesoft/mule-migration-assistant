/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.nocompatibility;

import com.google.common.collect.Lists;
import com.mulesoft.tools.migration.library.applicationgraph.ApplicationGraphCreator;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.applicationgraph.ApplicationGraph;
import com.mulesoft.tools.migration.tck.ReportVerification;
import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

@RunWith(Parameterized.class)
public class TranslateInboundReferencesStepTest {

  private static final Path CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/nocompatibility");

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private final Path configPath;
  private final Path targetPath;
  private Document doc;
  private ApplicationModel applicationModel;
  private ApplicationGraphCreator applicationGraphCreator;
  private TranslateInboundReferencesStep step;


  @Parameterized.Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "nocompatibility-01",
        "nocompatibility-02",
        "nocompatibility-03"
    };
  }

  public TranslateInboundReferencesStepTest(String filePrefix) {
    configPath = CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    applicationModel = mockApplicationModel(doc, temp);
    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(report.getReport(), applicationModel);
    applicationGraphCreator = new ApplicationGraphCreator();
    applicationGraphCreator.setExpressionMigrator(expressionMigrator);
    ApplicationGraph graph = applicationGraphCreator.create(Lists.newArrayList(doc), report.getReport());
    when(applicationModel.getApplicationGraph()).thenReturn(graph);
    step = new TranslateInboundReferencesStep();
    step.setExpressionMigrator(expressionMigrator);
  }

  @Test
  public void testInboundReferencesRemoved() throws URISyntaxException, IOException {
    step.execute(applicationModel, report.getReport());
    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);
    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
