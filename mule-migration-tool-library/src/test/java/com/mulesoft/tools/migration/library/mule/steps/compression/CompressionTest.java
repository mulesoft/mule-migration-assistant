/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.compression;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
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

@RunWith(Parameterized.class)
public class CompressionTest {

  private static final Path COMPRESSION_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/compression");

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  @Parameterized.Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "compression-01"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public CompressionTest(String filePrefix) {
    configPath = COMPRESSION_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = COMPRESSION_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private CompressionInlinerStep inliner = new CompressionInlinerStep();
  private GZipCompressTransformer compress = new GZipCompressTransformer();
  private GZipUncompressTransformer uncompress = new GZipUncompressTransformer();

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mock(ApplicationModel.class);
    when(appModel.getNode(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]).stream().findFirst()
            .orElse(null));
    when(appModel.getNodes(any(String.class))).thenAnswer(invocation -> getElementsFromDocument(doc, invocation.getArgument(0)));

    compress.setApplicationModel(appModel);
    uncompress.setApplicationModel(appModel);
    inliner.setApplicationModel(appModel);
  }

  @Test
  public void execute() throws Exception {
    runSteps(inliner, compress, uncompress);

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

  private void runSteps(AbstractApplicationModelMigrationStep... steps) {
    for (AbstractApplicationModelMigrationStep step : steps) {
      getElementsFromDocument(doc, step.getAppliedTo().getExpression()).forEach(node -> step.execute(node, report.getReport()));
    }
  }
}
