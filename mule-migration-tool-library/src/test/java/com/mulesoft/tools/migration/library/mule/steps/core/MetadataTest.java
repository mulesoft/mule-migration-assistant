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

import com.mulesoft.tools.migration.library.mule.steps.ee.EETransform;
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
public class MetadataTest {

  private static final Path CORE_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/studio");

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  @Parameterized.Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "metadata-01",
        "metadata-02"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public MetadataTest(String filePrefix) {
    configPath = CORE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = CORE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private SetPayload setPayload;
  private EETransform dwTransform;
  private RemoveMetadataAttributes metadata;

  @Before
  public void setUp() throws Exception {
    ApplicationModel appModel = mock(ApplicationModel.class);

    setPayload = new SetPayload();
    setPayload.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));
    dwTransform = new EETransform();
    dwTransform.setApplicationModel(appModel);
    metadata = new RemoveMetadataAttributes();
  }

  @Test
  public void execute() throws Exception {
    Document doc =
        getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    getElementsFromDocument(doc, setPayload.getAppliedTo().getExpression())
        .forEach(node -> setPayload.execute(node, report.getReport()));
    getElementsFromDocument(doc, dwTransform.getAppliedTo().getExpression())
        .forEach(node -> dwTransform.execute(node, report.getReport()));
    getElementsFromDocument(doc, metadata.getAppliedTo().getExpression())
        .forEach(node -> metadata.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
