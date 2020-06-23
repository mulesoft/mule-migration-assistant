/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.requestReply;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.core.GenericGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationAttributes;
import com.mulesoft.tools.migration.library.mule.steps.endpoint.RequestReply;
import com.mulesoft.tools.migration.library.mule.steps.jms.JmsConnector;
import com.mulesoft.tools.migration.library.mule.steps.jms.JmsGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.vm.VmConnector;
import com.mulesoft.tools.migration.library.mule.steps.vm.VmGlobalEndpoint;
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
public class RequestReplyTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path RR_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/requestReply");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "request-reply-01",
        "request-reply-02",
        "request-reply-03",
        "request-reply-04",
        "request-reply-05",
        "request-reply-06",
        "request-reply-07",
        "request-reply-08",
        "request-reply-09",
        "request-reply-10"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public RequestReplyTest(String jmsPrefix) {
    configPath = RR_CONFIG_EXAMPLES_PATH.resolve(jmsPrefix + "-original.xml");
    targetPath = RR_CONFIG_EXAMPLES_PATH.resolve(jmsPrefix + ".xml");
  }

  private GenericGlobalEndpoint genericGlobalEndpoint;
  private RequestReply requestReply;
  private JmsGlobalEndpoint jmsGlobalEndpoint;
  private JmsConnector jmsConfig;
  private VmGlobalEndpoint vmGlobalEndpoint;
  private VmConnector vmConfig;
  private RemoveSyntheticMigrationAttributes removeSyntheticMigrationAttributes;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);

    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(report.getReport(), appModel);

    genericGlobalEndpoint = new GenericGlobalEndpoint();
    genericGlobalEndpoint.setApplicationModel(appModel);

    requestReply = new RequestReply();
    requestReply.setApplicationModel(appModel);
    jmsGlobalEndpoint = new JmsGlobalEndpoint();
    jmsGlobalEndpoint.setApplicationModel(appModel);
    jmsConfig = new JmsConnector();
    jmsConfig.setApplicationModel(appModel);
    vmGlobalEndpoint = new VmGlobalEndpoint();
    vmGlobalEndpoint.setApplicationModel(appModel);
    vmConfig = new VmConnector();
    vmConfig.setApplicationModel(appModel);
    removeSyntheticMigrationAttributes = new RemoveSyntheticMigrationAttributes();
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, genericGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> genericGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, requestReply.getAppliedTo().getExpression())
        .forEach(node -> requestReply.execute(node, report.getReport()));
    getElementsFromDocument(doc, jmsGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> jmsGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, jmsConfig.getAppliedTo().getExpression())
        .forEach(node -> jmsConfig.execute(node, report.getReport()));
    getElementsFromDocument(doc, vmGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> vmGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, vmConfig.getAppliedTo().getExpression())
        .forEach(node -> vmConfig.execute(node, report.getReport()));
    getElementsFromDocument(doc, removeSyntheticMigrationAttributes.getAppliedTo().getExpression())
        .forEach(node -> removeSyntheticMigrationAttributes.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
