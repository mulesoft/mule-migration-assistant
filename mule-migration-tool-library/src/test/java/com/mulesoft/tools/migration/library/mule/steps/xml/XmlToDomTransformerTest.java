/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.xml;

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

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

@RunWith(Parameterized.class)
public class XmlToDomTransformerTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path XML_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/xml");

  @Parameterized.Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "xml-to-dom-01"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public XmlToDomTransformerTest(String xmlPrefix) {
    configPath = XML_CONFIG_EXAMPLES_PATH.resolve(xmlPrefix + "-original.xml");
    targetPath = XML_CONFIG_EXAMPLES_PATH.resolve(xmlPrefix + ".xml");
  }

  private XmlToDomTransformer xmlToDomTransformer;

  @Before
  public void setUp() throws Exception {
    xmlToDomTransformer = new XmlToDomTransformer();
  }

  @Test
  public void execute() throws Exception {
    Document doc =
        getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    getElementsFromDocument(doc, xmlToDomTransformer.getAppliedTo().getExpression())
        .forEach(node -> xmlToDomTransformer.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
    report.expectReportEntry("mulexml.xmlToDom");
  }
}
