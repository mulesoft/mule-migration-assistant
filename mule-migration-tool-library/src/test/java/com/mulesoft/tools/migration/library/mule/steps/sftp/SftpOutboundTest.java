/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.sftp;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.core.GenericGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationAttributes;
import com.mulesoft.tools.migration.library.mule.steps.endpoint.OutboundEndpoint;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

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
public class SftpOutboundTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path SFTP_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/sftp");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "sftp-outbound-01",
        // TODO MMT-260
        // "sftp-outbound-02",
        // "sftp-outbound-03",
        "sftp-outbound-04",
        "sftp-outbound-05",
        "sftp-outbound-06",
        "sftp-outbound-07",
        "sftp-outbound-08",
        "sftp-outbound-09",
        "sftp-outbound-10"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public SftpOutboundTest(String filePrefix) {
    configPath = SFTP_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = SFTP_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private GenericGlobalEndpoint genericGlobalEndpoint;
  private SftpGlobalEndpoint sftpGlobalEndpoint;
  private SftpConfig sftpConfig;
  private SftpOutboundEndpoint sftpOutboundEndpoint;
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

    sftpGlobalEndpoint = new SftpGlobalEndpoint();
    sftpGlobalEndpoint.setApplicationModel(appModel);
    sftpConfig = new SftpConfig();
    sftpConfig.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), mock(ApplicationModel.class)));
    sftpConfig.setApplicationModel(appModel);
    sftpOutboundEndpoint = new SftpOutboundEndpoint();
    sftpOutboundEndpoint.setApplicationModel(appModel);
    sftpOutboundEndpoint
        .setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), mock(ApplicationModel.class)));
    outboundEndpoint = new OutboundEndpoint();
    // inboundEndpoint.setExpressionMigrator(expressionMigrator);
    outboundEndpoint.setApplicationModel(appModel);
    removeSyntheticMigrationAttributes = new RemoveSyntheticMigrationAttributes();
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, genericGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> genericGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, sftpGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> sftpGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, sftpConfig.getAppliedTo().getExpression())
        .forEach(node -> sftpConfig.execute(node, report.getReport()));
    getElementsFromDocument(doc, sftpOutboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> sftpOutboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, outboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> outboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, removeSyntheticMigrationAttributes.getAppliedTo().getExpression())
        .forEach(node -> removeSyntheticMigrationAttributes.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
