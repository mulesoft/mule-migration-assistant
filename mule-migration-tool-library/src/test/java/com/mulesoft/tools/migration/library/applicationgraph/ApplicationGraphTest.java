/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.applicationgraph;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.applicationgraph.ApplicationGraph;
import com.mulesoft.tools.migration.project.model.applicationgraph.FlowComponent;
import com.mulesoft.tools.migration.project.model.applicationgraph.MessageProcessor;
import com.mulesoft.tools.migration.tck.ReportVerification;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static org.junit.Assert.*;

public class ApplicationGraphTest {

  private static final Path CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/nocompatibility");

  @Rule
  public ReportVerification report = new ReportVerification();
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private final Path configPath;
  private final Path targetPath;
  private Document doc;
  private ApplicationGraphCreator applicationGraphCreator;
  private ApplicationGraph graph;

  public ApplicationGraphTest() {
    configPath = CONFIG_EXAMPLES_PATH.resolve("nocompatibility-01-original.xml");
    targetPath = CONFIG_EXAMPLES_PATH.resolve("nocompatibility-01.xml");
  }

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    ApplicationModel applicationModel = mockApplicationModel(doc, temp);
    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(report.getReport(), applicationModel);
    applicationGraphCreator = new ApplicationGraphCreator();
    applicationGraphCreator.setExpressionMigrator(expressionMigrator);
    graph = applicationGraphCreator.create(Lists.newArrayList(doc), report.getReport());
  }

  @Test
  public void testAbleToFindGraphComponentFromXMLElement() {
    String loggerSubflow3Expression = "//*[local-name()='sub-flow' and @name='flow3']/*[local-name()='logger']";
    Element loggerElement =
        Iterables.getOnlyElement(XPathFactory.instance().compile(loggerSubflow3Expression, Filters.element()).evaluate(doc));

    FlowComponent flowComponent = graph.findFlowComponent(loggerElement);
    assertNotNull(flowComponent);
    assertTrue(flowComponent instanceof MessageProcessor);
  }

  @Test
  public void testAbleToFindGraphComponentFromXMLElement_afterModification() {
    String loggerSubflow3Expression = "//*[local-name()='sub-flow' and @name='flow3']/*[local-name()='logger']";
    Element loggerElement =
        Iterables.getOnlyElement(XPathFactory.instance().compile(loggerSubflow3Expression, Filters.element()).evaluate(doc));

    FlowComponent initialFlowComponent = graph.findFlowComponent(loggerElement);
    loggerElement.setName("customLogger");

    graph.findFlowComponent(loggerElement);
    FlowComponent flowComponentAfterModification = graph.findFlowComponent(loggerElement);
    assertEquals(initialFlowComponent, flowComponentAfterModification);
  }

  @Test
  public void testAbleToFindGraphComponentFromXMLElement_afterDetaching() {
    String loggerSubflow3Expression = "//*[local-name()='sub-flow' and @name='flow3']/*[local-name()='logger']";
    Element loggerElement =
        Iterables.getOnlyElement(XPathFactory.instance().compile(loggerSubflow3Expression, Filters.element()).evaluate(doc));

    FlowComponent initialFlowComponent = graph.findFlowComponent(loggerElement);
    loggerElement.detach();

    graph.findFlowComponent(loggerElement);
    FlowComponent flowComponentAfterModification = graph.findFlowComponent(loggerElement);
    assertEquals(initialFlowComponent, flowComponentAfterModification);
  }
}
