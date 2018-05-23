/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.file;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(Parameterized.class)
public class FileConfigTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private static final Path FILE_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/file");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "file-config-01",
        // TODO service-overrides
        // "file-config-02",
        "file-config-03",
        "file-config-04",
        "file-config-05",
        "file-config-06",
        "file-config-07",
        "file-config-08",
        "file-config-09"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public FileConfigTest(String filePrefix) {
    configPath = FILE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = FILE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private FileGlobalEndpoint fileGlobalEndpoint;
  private FileConfig fileConfig;
  private FileInboundEndpoint fileInboundEndpoint;
  private FileOutboundEndpoint fileOutboundEndpoint;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());

    appModel = mock(ApplicationModel.class);
    when(appModel.getNodes(Mockito.any(XPathExpression.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc,
                                                          ((XPathExpression) (invocation.getArguments()[0])).getExpression()));
    when(appModel.getProjectBasePath()).thenReturn(temp.newFolder().toPath());

    fileGlobalEndpoint = new FileGlobalEndpoint();
    fileConfig = new FileConfig();
    fileConfig.setExpressionMigrator(new MelToDwExpressionMigrator(mock(MigrationReport.class)));
    fileConfig.setApplicationModel(appModel);
    fileInboundEndpoint = new FileInboundEndpoint();
    fileInboundEndpoint.setApplicationModel(appModel);
    fileOutboundEndpoint = new FileOutboundEndpoint();
    fileOutboundEndpoint.setApplicationModel(appModel);
    fileOutboundEndpoint.setExpressionMigrator(new MelToDwExpressionMigrator(mock(MigrationReport.class)));
  }

  @Ignore
  @Test(expected = MigrationStepException.class)
  public void executeWithNullElement() throws Exception {
    fileConfig.execute(null, mock(MigrationReport.class));
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, fileGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> fileGlobalEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, fileConfig.getAppliedTo().getExpression())
        .forEach(node -> fileConfig.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, fileInboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> fileInboundEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, fileOutboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> fileOutboundEndpoint.execute(node, mock(MigrationReport.class)));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
