/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
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

public class ChoiceExpressionsTest {

  private static final String CHOICE_SAMPLE_XML = "choice-original.xml";
  private static final String CHOICE_EXPECTED_XML = "choice.xml";
  private static final Path CHOICE_EXAMPLES_PATH = Paths.get("mule/apps/core");
  private static final Path CHOICE_SAMPLE_PATH = CHOICE_EXAMPLES_PATH.resolve(CHOICE_SAMPLE_XML);
  private static final Path CHOICE_EXPECTED_PATH = CHOICE_EXAMPLES_PATH.resolve(CHOICE_EXPECTED_XML);

  @Rule
  public ReportVerification report = new ReportVerification();

  private ApplicationModel appModel;

  private ChoiceExpressions choiceExpressions;
  private Element node;

  @Before
  public void setUp() throws Exception {
    appModel = mock(ApplicationModel.class);

    choiceExpressions = new ChoiceExpressions();
    choiceExpressions.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));
    choiceExpressions.setApplicationModel(appModel);
  }


  @Test
  public void execute() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(CHOICE_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, choiceExpressions.getAppliedTo().getExpression()).get(0);
    choiceExpressions.execute(node, report.getReport());

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(CHOICE_EXPECTED_PATH.toString()).toURI(),
                                            UTF_8))
                                                .ignoreComments().normalizeWhitespace());
  }
}
