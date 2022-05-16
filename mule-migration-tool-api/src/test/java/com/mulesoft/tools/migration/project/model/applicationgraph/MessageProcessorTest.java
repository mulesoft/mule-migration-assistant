/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class MessageProcessorTest {

  private ApplicationGraph applicationGraph;

  @Test
  public void testNameCreation_DuplicatedElements_sameFlow() {
    applicationGraph = new ApplicationGraph();
    Element flowElement = new Element("flow");
    flowElement.setAttribute(new Attribute("name", "flow1"));
    Flow parentFlow = new Flow(flowElement);
    applicationGraph.addFlowComponent(createLogger(parentFlow, applicationGraph));
    applicationGraph.addFlowComponent(createLogger(parentFlow, applicationGraph));

    assertThat(applicationGraph.getAllVertexNamesWithBaseName("logger_flow1"),
               containsInAnyOrder("logger_flow1", "logger_flow1-1"));
  }

  @Test
  public void testNameCreation_TriplicatedElements_sameFlow() {
    applicationGraph = new ApplicationGraph();
    Element flowElement = new Element("flow");
    flowElement.setAttribute(new Attribute("name", "flow1"));
    Flow parentFlow = new Flow(flowElement);
    applicationGraph.addFlowComponent(createLogger(parentFlow, applicationGraph));
    applicationGraph.addFlowComponent(createLogger(parentFlow, applicationGraph));
    applicationGraph.addFlowComponent(createLogger(parentFlow, applicationGraph));

    assertThat(applicationGraph.getAllVertexNamesWithBaseName("logger_flow1"),
               containsInAnyOrder("logger_flow1", "logger_flow1-1", "logger_flow1-2"));
  }

  @Test
  public void testNameCreation_sameElements_differentFlowsWithSamePrefix() {
    applicationGraph = new ApplicationGraph();
    Element flowElement = new Element("flow");
    flowElement.setAttribute(new Attribute("name", "flow1"));
    Flow parentFlow = new Flow(flowElement);
    Element flowElement2 = new Element("flow");
    flowElement2.setAttribute(new Attribute("name", "flow12"));
    Flow parentFlow2 = new Flow(flowElement2);
    applicationGraph.addFlowComponent(createLogger(parentFlow, applicationGraph));
    applicationGraph.addFlowComponent(createLogger(parentFlow2, applicationGraph));

    assertThat(applicationGraph.getAllVertexNamesWithBaseName("logger_flow1"),
               containsInAnyOrder("logger_flow1"));

    assertThat(applicationGraph.getAllVertexNamesWithBaseName("logger_flow12"),
               containsInAnyOrder("logger_flow12"));
  }

  @Test
  public void testNameCreation_elementsWithDashes() {
    applicationGraph = new ApplicationGraph();
    Element flowElement = new Element("flow");
    flowElement.setAttribute(new Attribute("name", "flow1"));
    Flow parentFlow = new Flow(flowElement);
    applicationGraph.addFlowComponent(createLogger("element-1", parentFlow, applicationGraph));
    applicationGraph.addFlowComponent(createLogger("element-1", parentFlow, applicationGraph));

    assertThat(applicationGraph.getAllVertexNamesWithBaseName("element-1_flow1"),
               containsInAnyOrder("element-1_flow1", "element-1_flow1-1"));
  }

  private FlowComponent createLogger(String elementName, Flow parentFlow, ApplicationGraph applicationGraph) {
    Element element = new Element(elementName);
    parentFlow.getXmlElement().addContent(element);

    return new MessageProcessor(element, parentFlow, applicationGraph);
  }

  private FlowComponent createLogger(Flow parentFlow, ApplicationGraph applicationGraph) {
    return createLogger("logger", parentFlow, applicationGraph);
  }

}
