/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.ee;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(Parameterized.class)
public class TrackingTest {

  private static final Path EE_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/ee");


  @Parameterized.Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "tracking-01",
        "tracking-02",
        // TODO Fixme willy!
        // "tracking-03",
        "tracking-04",
        "tracking-05",
        "tracking-06",
        "tracking-07",
        "tracking-08",
        "tracking-09"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public TrackingTest(String filePrefix) {
    configPath = EE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = EE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private Tracking tracking;

  @Before
  public void setUp() throws Exception {
    tracking = new Tracking();
    ApplicationModel appModel = mock(ApplicationModel.class);
    tracking.setExpressionMigrator(new MelToDwExpressionMigrator(mock(MigrationReport.class), appModel));
    tracking.setApplicationModel(appModel);
  }

  @Test
  public void execute() throws Exception {
    Document doc =
        getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    getElementsFromDocument(doc, tracking.getAppliedTo().getExpression())
        .forEach(node -> tracking.execute(node, mock(MigrationReport.class)));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
