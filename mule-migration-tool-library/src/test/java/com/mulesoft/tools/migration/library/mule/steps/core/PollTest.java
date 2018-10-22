/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.tck.ReportVerification;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PollTest {

  private static final String FILE_SAMPLE_XML = "poll.xml";
  private static final Path FILE_EXAMPLES_PATH = Paths.get("mule/examples/core");
  private static final Path FILE_SAMPLE_PATH = FILE_EXAMPLES_PATH.resolve(FILE_SAMPLE_XML);
  private static final String SCHEDULING_STRATEGY = "scheduling-strategy";
  private static final String CORE_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/core";
  private static final String CORE_NAME = "mule";
  private static final Namespace CORE_NAMESPACE = Namespace.getNamespace(CORE_NAME, CORE_NAMESPACE_URI);

  @Rule
  public ReportVerification report = new ReportVerification();

  private Poll poll;
  private Element node;
  private Document doc;

  @Before
  public void setUp() throws Exception {
    poll = new Poll();
    doc = getDocument(this.getClass().getClassLoader().getResource(FILE_SAMPLE_PATH.toString()).toURI().getPath());
  }

  @Test(expected = MigrationStepException.class)
  public void executeWithNullElement() throws Exception {
    poll.execute(null, report.getReport());
  }

  @Test
  public void executeChangePollName() throws Exception {
    node = getElementsFromDocument(doc, poll.getAppliedTo().getExpression()).get(0);
    poll.execute(node, report.getReport());

    assertThat("The node didn't change", node.getName(), is("scheduler"));
  }

  @Test
  public void executeMoveElementsInsideProcessorChain() throws Exception {
    node = getElementsFromDocument(doc, poll.getAppliedTo().getExpression()).get(1);
    poll.execute(node, report.getReport());

    assertThat("The child elements weren't moved", node.getParentElement().getChildren().size(), is(4));
    assertThat("The child elements weren't moved", node.getParentElement().getChildren().get(3).getName(), is("set-payload"));
  }

  @Test
  public void executeMoveElements() throws Exception {
    node = getElementsFromDocument(doc, poll.getAppliedTo().getExpression()).get(2);
    poll.execute(node, report.getReport());

    assertThat("The child elements weren't moved", node.getParentElement().getChildren().size(), is(2));
    assertThat("The child elements weren't moved", node.getParentElement().getChildren().get(1).getName(), is("logger"));
  }

  @Test
  public void executeCronConfiguration() throws Exception {
    node = getElementsFromDocument(doc, poll.getAppliedTo().getExpression()).get(2);
    poll.execute(node, report.getReport());

    Element cronConfiguration = node.getChild(SCHEDULING_STRATEGY, CORE_NAMESPACE).getChildren().get(0);

    assertThat("The child elements weren't moved", cronConfiguration.getName(), is("cron"));
    assertThat("The child elements weren't moved", cronConfiguration.getAttribute("timeZone"), is(notNullValue()));
  }

  @Test
  public void executeFixedFrequencyConfiguration() throws Exception {
    node = getElementsFromDocument(doc, poll.getAppliedTo().getExpression()).get(1);
    poll.execute(node, report.getReport());

    Element fixedFrequencyConfiguration = node.getChild(SCHEDULING_STRATEGY, CORE_NAMESPACE).getChildren().get(0);

    assertThat("The child elements weren't moved", fixedFrequencyConfiguration.getName(), is("fixed-frequency"));
    assertThat("The child elements weren't moved", fixedFrequencyConfiguration.getAttributes().size(), is(1));
    assertThat("The child elements weren't moved", fixedFrequencyConfiguration.getAttribute("frequency"), is(notNullValue()));
  }

  @Test
  public void executeFixedFrequencyAllFieldsConfiguration() throws Exception {
    node = getElementsFromDocument(doc, poll.getAppliedTo().getExpression()).get(3);
    poll.execute(node, report.getReport());

    Element fixedFrequencyConfiguration = node.getChild(SCHEDULING_STRATEGY, CORE_NAMESPACE).getChildren().get(0);

    assertThat("The child elements weren't moved", fixedFrequencyConfiguration.getName(), is("fixed-frequency"));
    assertThat("The child elements weren't moved", fixedFrequencyConfiguration.getAttributes().size(), is(3));
  }

  @Test
  public void executeWithContenAfterPoll() throws Exception {
    node = getElementsFromDocument(doc, poll.getAppliedTo().getExpression()).get(4);
    poll.execute(node, report.getReport());

    assertThat("The child elements weren't moved", node.getParentElement().getChildren().get(0).getName(), is("scheduler"));
    assertThat("The child elements weren't moved", node.getParentElement().getChildren().get(2).getName(), is("set-payload"));
  }
}
