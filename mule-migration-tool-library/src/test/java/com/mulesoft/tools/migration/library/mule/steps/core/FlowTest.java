/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

import java.nio.file.Path;
import java.nio.file.Paths;

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
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class FlowTest {

  private static final Path FLOW_EXAMPLES_PATH = Paths.get("mule/apps/core");

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "flow-01",
        "flow-02",
        "flow-03",
        "flow-04",
        "flow-05",
        "flow-06",
        "flow-07",
        "flow-08",
        "flow-09",
        "flow-10",
        "flow-11",
        "flow-12",
        "flow-13",
        "flow-14",
        "flow-15",
        "flow-16",
        "flow-17-multiple-response-elements",
        "flow-18-response-error-handling"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public FlowTest(String filePrefix) {
    configPath = FLOW_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = FLOW_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private Flow flow;
  private SubFlow subFlow;
  private FlowRef flowRef;
  private CatchExceptionStrategy exceptionStrategy;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);

    flow = new Flow();
    flow.setApplicationModel(appModel);
    subFlow = new SubFlow();
    flowRef = new FlowRef();
    flowRef.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));
    exceptionStrategy = new CatchExceptionStrategy();
    exceptionStrategy.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));
  }


  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, exceptionStrategy.getAppliedTo().getExpression())
        .forEach(node -> exceptionStrategy.execute(node, report.getReport()));
    getElementsFromDocument(doc, flow.getAppliedTo().getExpression())
        .forEach(node -> flow.execute(node, report.getReport()));
    getElementsFromDocument(doc, subFlow.getAppliedTo().getExpression())
        .forEach(node -> subFlow.execute(node, report.getReport()));
    getElementsFromDocument(doc, flowRef.getAppliedTo().getExpression())
        .forEach(node -> flowRef.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils
                   .toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                       .ignoreComments().normalizeWhitespace());
  }

}
