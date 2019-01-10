/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.vm;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.core.GenericGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.endpoint.OutboundEndpoint;
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
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(Parameterized.class)
public class VmOutboundTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path VM_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/vm");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "vm-outbound-01",
        "vm-outbound-02",
        "vm-outbound-03",
        "vm-outbound-04",
        "vm-outbound-05",
        "vm-outbound-06",
        "vm-outbound-07",
        "vm-outbound-08",
        "vm-outbound-09",
        "vm-outbound-10",
        "vm-outbound-11",
        "vm-outbound-12",
        "vm-outbound-13",
        "vm-outbound-13b",
        "vm-outbound-14",
        "vm-outbound-15"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public VmOutboundTest(String vmPrefix) {
    configPath = VM_CONFIG_EXAMPLES_PATH.resolve(vmPrefix + "-original.xml");
    targetPath = VM_CONFIG_EXAMPLES_PATH.resolve(vmPrefix + ".xml");
  }

  private GenericGlobalEndpoint genericGlobalEndpoint;
  private VmGlobalEndpoint vmGlobalEndpoint;
  private VmOutboundEndpoint vmOutboundEndpoint;
  private OutboundEndpoint outboundEndpoint;
  private VmConnector vmConfig;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);

    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(report.getReport(), appModel);

    genericGlobalEndpoint = new GenericGlobalEndpoint();
    genericGlobalEndpoint.setApplicationModel(appModel);

    vmGlobalEndpoint = new VmGlobalEndpoint();
    vmGlobalEndpoint.setApplicationModel(appModel);

    vmOutboundEndpoint = new VmOutboundEndpoint();
    vmOutboundEndpoint.setApplicationModel(appModel);
    vmOutboundEndpoint.setExpressionMigrator(expressionMigrator);
    outboundEndpoint = new OutboundEndpoint();
    outboundEndpoint.setApplicationModel(appModel);
    outboundEndpoint.setExpressionMigrator(expressionMigrator);
    vmConfig = new VmConnector();
    vmConfig.setApplicationModel(appModel);
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, genericGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> genericGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, vmGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> vmGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, vmOutboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> vmOutboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, outboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> outboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, vmConfig.getAppliedTo().getExpression())
        .forEach(node -> vmConfig.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
