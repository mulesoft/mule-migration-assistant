/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
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

import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(Parameterized.class)
public class PollTest {

  private static final Path CORE_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/poll");

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  @Parameterized.Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "poll-01",
        "poll-02",
        "poll-03",
        "poll-04",
        "poll-05",
        "poll-06",
        "poll-07",
        "poll-08",
        "poll-watermark-01",
        "poll-watermark-02",
        "poll-watermark-03",
        "poll-watermark-04",
        "poll-watermark-05",
        "poll-watermark-06",
        "poll-watermark-07",
        "poll-watermark-08",
        "poll-watermark-09",
        "poll-watermark-10",
        "poll-watermark-11",
        "poll-watermark-12"

    };
  }

  private final Path configPath;
  private final Path targetPath;

  public PollTest(String filePrefix) {
    configPath = CORE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = CORE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private Poll poll;
  private RemoveSyntheticMigrationAttributes removeSyntheticMigrationAttributes;
  private KeepElementsAtBottomOfFlow keepElementsAtBottomOfFlow;

  @Before
  public void setUp() throws Exception {
    poll = new Poll();
    poll.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), mock(ApplicationModel.class)));
    poll.setApplicationModel(mock(ApplicationModel.class));
    removeSyntheticMigrationAttributes = new RemoveSyntheticMigrationAttributes();
    keepElementsAtBottomOfFlow = new KeepElementsAtBottomOfFlow();
  }

  @Test
  public void execute() throws Exception {
    Document doc =
        getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    getElementsFromDocument(doc, poll.getAppliedTo().getExpression())
        .forEach(node -> poll.execute(node, report.getReport()));
    getElementsFromDocument(doc, removeSyntheticMigrationAttributes.getAppliedTo().getExpression())
        .forEach(node -> removeSyntheticMigrationAttributes.execute(node, report.getReport()));
    getElementsFromDocument(doc, keepElementsAtBottomOfFlow.getAppliedTo().getExpression())
        .forEach(node -> keepElementsAtBottomOfFlow.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
