/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.ftp;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.core.GenericGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationAttributes;
import com.mulesoft.tools.migration.library.mule.steps.endpoint.OutboundEndpoint;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RunWith(Parameterized.class)
public class FtpOutboundTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private static final Path FTP_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/ftp");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "ftp-outbound-01",
        "ftp-outbound-02",
        "ftp-outbound-03",
        "ftp-outbound-04"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public FtpOutboundTest(String filePrefix) {
    configPath = FTP_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = FTP_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private GenericGlobalEndpoint genericGlobalEndpoint;
  private FtpGlobalEndpoint ftpGlobalEndpoint;
  private FtpEeGlobalEndpoint ftpEeGlobalEndpoint;
  private FtpConfig ftpConfig;
  private FtpEeConfig ftpEeConfig;
  private FtpOutboundEndpoint ftpOutboundEndpoint;
  private FtpEeOutboundEndpoint ftpEeOutboundEndpoint;
  private OutboundEndpoint outboundEndpoint;
  private RemoveSyntheticMigrationAttributes removeSyntheticMigrationAttributes;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());

    appModel = mock(ApplicationModel.class);
    when(appModel.getNodes(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]));
    when(appModel.getNode(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]).iterator().next());
    when(appModel.getNodeOptional(any(String.class)))
        .thenAnswer(invocation -> {
          List<Element> elementsFromDocument = getElementsFromDocument(doc, (String) invocation.getArguments()[0]);
          if (elementsFromDocument.isEmpty()) {
            return empty();
          } else {
            return of(elementsFromDocument.iterator().next());
          }
        });
    when(appModel.getProjectBasePath()).thenReturn(temp.newFolder().toPath());

    genericGlobalEndpoint = new GenericGlobalEndpoint();
    genericGlobalEndpoint.setApplicationModel(appModel);

    ftpGlobalEndpoint = new FtpGlobalEndpoint();
    ftpGlobalEndpoint.setApplicationModel(appModel);
    ftpEeGlobalEndpoint = new FtpEeGlobalEndpoint();
    ftpEeGlobalEndpoint.setApplicationModel(appModel);
    ftpConfig = new FtpConfig();
    ftpConfig.setExpressionMigrator(new MelToDwExpressionMigrator(mock(MigrationReport.class), mock(ApplicationModel.class)));
    ftpConfig.setApplicationModel(appModel);
    ftpEeConfig = new FtpEeConfig();
    ftpEeConfig.setExpressionMigrator(new MelToDwExpressionMigrator(mock(MigrationReport.class), mock(ApplicationModel.class)));
    ftpEeConfig.setApplicationModel(appModel);
    ftpOutboundEndpoint = new FtpOutboundEndpoint();
    ftpOutboundEndpoint.setApplicationModel(appModel);
    ftpOutboundEndpoint
        .setExpressionMigrator(new MelToDwExpressionMigrator(mock(MigrationReport.class), mock(ApplicationModel.class)));
    ftpEeOutboundEndpoint = new FtpEeOutboundEndpoint();
    ftpEeOutboundEndpoint.setApplicationModel(appModel);
    ftpEeOutboundEndpoint
        .setExpressionMigrator(new MelToDwExpressionMigrator(mock(MigrationReport.class), mock(ApplicationModel.class)));
    outboundEndpoint = new OutboundEndpoint();
    // inboundEndpoint.setExpressionMigrator(expressionMigrator);
    outboundEndpoint.setApplicationModel(appModel);
    removeSyntheticMigrationAttributes = new RemoveSyntheticMigrationAttributes();
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, genericGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> genericGlobalEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, ftpGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> ftpGlobalEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, ftpEeGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> ftpEeGlobalEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, ftpConfig.getAppliedTo().getExpression())
        .forEach(node -> ftpConfig.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, ftpEeConfig.getAppliedTo().getExpression())
        .forEach(node -> ftpEeConfig.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, ftpOutboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> ftpOutboundEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, ftpEeOutboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> ftpEeOutboundEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, outboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> outboundEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, removeSyntheticMigrationAttributes.getAppliedTo().getExpression())
        .forEach(node -> removeSyntheticMigrationAttributes.execute(node, mock(MigrationReport.class)));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
