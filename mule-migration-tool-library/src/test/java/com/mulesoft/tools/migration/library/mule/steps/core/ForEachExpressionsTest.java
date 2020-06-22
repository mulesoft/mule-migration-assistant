/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

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

import java.nio.file.Path;
import java.nio.file.Paths;

public class ForEachExpressionsTest {

  private static final String FOREACH_SAMPLE_XML = "foreach-original.xml";
  private static final String FOREACH_EXPECTED_XML = "foreach.xml";
  private static final Path FOREACH_EXAMPLES_PATH = Paths.get("mule/apps/core");
  private static final Path FOREACH_SAMPLE_PATH = FOREACH_EXAMPLES_PATH.resolve(FOREACH_SAMPLE_XML);
  private static final Path FOREACH_EXPECTED_PATH = FOREACH_EXAMPLES_PATH.resolve(FOREACH_EXPECTED_XML);

  @Rule
  public ReportVerification report = new ReportVerification();

  private ForEachExpressions forEachExpressions;
  private Element node;

  @Before
  public void setUp() throws Exception {
    forEachExpressions = new ForEachExpressions();
    forEachExpressions
        .setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), mock(ApplicationModel.class)));
  }


  @Test
  public void execute() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(FOREACH_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, forEachExpressions.getAppliedTo().getExpression()).get(0);
    forEachExpressions.execute(node, report.getReport());

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils
                   .toString(this.getClass().getClassLoader().getResource(FOREACH_EXPECTED_PATH.toString()).toURI(), UTF_8))
                       .ignoreComments().normalizeWhitespace());
  }

}
