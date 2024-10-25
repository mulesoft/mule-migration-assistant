/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

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
public class AttachmentsTest {

  private static final Path FLOW_EXAMPLES_PATH = Paths.get("mule/apps/core");

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "attachments-01",
        "attachments-02",
        "attachments-03",
        "attachments-04",
        "attachments-05",
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public AttachmentsTest(String filePrefix) {
    configPath = FLOW_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = FLOW_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private SetAttachment setAttachment;
  private RemoveAttachment removeAttachment;
  private CopyAttachments copyAttachments;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    setAttachment = new SetAttachment();
    removeAttachment = new RemoveAttachment();

    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);

    copyAttachments = new CopyAttachments();
    copyAttachments.setApplicationModel(appModel);
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, setAttachment.getAppliedTo().getExpression())
        .forEach(node -> setAttachment.execute(node, report.getReport()));
    getElementsFromDocument(doc, removeAttachment.getAppliedTo().getExpression())
        .forEach(node -> removeAttachment.execute(node, report.getReport()));
    getElementsFromDocument(doc, copyAttachments.getAppliedTo().getExpression())
        .forEach(node -> copyAttachments.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils
                   .toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                       .ignoreComments().normalizeWhitespace());
  }
}
