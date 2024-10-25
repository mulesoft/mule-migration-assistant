/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.compression;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
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

  private final CompressionInlinerStep inliner = new CompressionInlinerStep();
  private final GZipCompressTransformer compress = new GZipCompressTransformer();
  private final GZipUncompressTransformer uncompress = new GZipUncompressTransformer();

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);

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
