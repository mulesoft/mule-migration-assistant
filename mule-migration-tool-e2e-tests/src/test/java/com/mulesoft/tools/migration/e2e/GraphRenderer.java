/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.e2e;

import static com.mulesoft.tools.migration.engine.project.MuleProjectFactory.getMuleProject;
import static com.mulesoft.tools.migration.engine.project.structure.BasicProject.getFiles;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.engine.project.ProjectTypeFactory;
import com.mulesoft.tools.migration.engine.project.structure.mule.MuleProject;
import com.mulesoft.tools.migration.library.applicationgraph.ApplicationGraphCreator;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.applicationgraph.ApplicationGraph;
import com.mulesoft.tools.migration.project.model.applicationgraph.FlowComponent;
import com.mulesoft.tools.migration.project.model.applicationgraph.DummyFlowRefReturnComponent;
import com.mulesoft.tools.migration.project.model.applicationgraph.DummyFlowTerminalComponent;
import com.mulesoft.tools.migration.project.model.applicationgraph.FlowRefFlowComponent;
import com.mulesoft.tools.migration.project.model.applicationgraph.MessageSourceFlowComponent;
import com.mulesoft.tools.migration.project.model.applicationgraph.OperationSourceFlowComponent;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizJdkEngine;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.ExportException;
import org.jgrapht.nio.dot.DOTExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util class to render an application graph
 *
 * @author Mulesoft Inc.
 * @since 1.4.0
 */
public class GraphRenderer {

  private static final Logger logger = LoggerFactory.getLogger(GraphRenderer.class);

  static {
    Graphviz.useEngine(new GraphvizJdkEngine());
  }

  private static void render(ApplicationGraph applicationGraph, String filePrefix) throws IOException {
    String dot = generateDot(getGraph(applicationGraph));
    logger.info("\n" + filePrefix + ".dot:\n" + dot);
    MutableGraph g = new Parser().read(dot);
    File targetFile = new File(String.format("target/graphs/%s.png", filePrefix));
    Graphviz.fromGraph(g).width(1280).render(Format.PNG)
        .toFile(targetFile);
  }

  private static Graph<FlowComponent, DefaultWeightedEdge> getGraph(ApplicationGraph applicationGraph) {
    Map<String, FlowComponent> flowComponentIds = applicationGraph.getFlowComponentIds();
    DefaultDirectedGraph<FlowComponent, DefaultWeightedEdge> graph =
        new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    for (FlowComponent fc : flowComponentIds.values()) {
      graph.addVertex(fc);
    }
    for (FlowComponent source : flowComponentIds.values()) {
      for (FlowComponent target : source.next()) {
        if (!graph.containsEdge(source, target)) {
          graph.addEdge(source, target);
        }
      }
    }
    return graph;
  }

  private static String generateDot(Graph<FlowComponent, DefaultWeightedEdge> stringGraph)
      throws ExportException {
    DOTExporter<FlowComponent, DefaultWeightedEdge> exporter = new DOTExporter<>(v -> GraphRenderer.getElementName(v));
    exporter.setVertexAttributeProvider((v) -> {
      Map<String, Attribute> map = new LinkedHashMap<>();
      map.put("style", DefaultAttribute.createAttribute("filled"));
      if (v instanceof MessageSourceFlowComponent) {
        map.put("fillcolor", DefaultAttribute.createAttribute("cadetBlue3"));
      } else if (v instanceof OperationSourceFlowComponent) {
        map.put("fillcolor", DefaultAttribute.createAttribute("lightPink"));
      } else if (v instanceof FlowRefFlowComponent) {
        map.put("fillcolor", DefaultAttribute.createAttribute("sandyBrown"));
      } else if (v instanceof DummyFlowRefReturnComponent) {
        map.put("fillcolor", DefaultAttribute.createAttribute("tan"));
      } else if (v instanceof DummyFlowTerminalComponent) {
        map.put("fillcolor", DefaultAttribute.createAttribute("violet"));
      }
      map.put("label",
              DefaultAttribute.createAttribute(String.format("%s\n(%s)\n%s",
                                                             v.getClass().getSimpleName(), getElementName(v),
                                                             v.getPropertiesMigrationContext() != null
                                                                 ? v.getPropertiesMigrationContext()
                                                                 : "[]")));
      return map;
    });
    Writer writer = new StringWriter();
    exporter.exportGraph(stringGraph, writer);
    return writer.toString();
  }

  private static String getElementName(FlowComponent v) {
    return v.getName().replaceAll("[-/:{}]", "_");
  }

  public static void render(String artifactName) throws Exception {

    URI uri = GraphRenderer.class.getClassLoader().getResource("e2e/" + artifactName + "/input").toURI();
    Path projectBasePath = new File(uri).toPath();

    ProjectTypeFactory projectFactory = new ProjectTypeFactory();
    ProjectType type = projectFactory.getProjectType(projectBasePath);
    MuleProject muleProject = getMuleProject(projectBasePath, type);

    ApplicationModel applicationModel = new ApplicationModel.ApplicationModelBuilder()
        .withProjectBasePath(projectBasePath)
        .withProjectType(type)
        .withConfigurationFiles(getFiles(muleProject.srcMainConfiguration(), "xml"))
        .withGenerateElementIds(true)
        .build();

    MigrationReport report = mock(MigrationReport.class);
    ApplicationGraphCreator applicationGraphCreator = new ApplicationGraphCreator();
    ApplicationGraph applicationGraph = applicationGraphCreator.create(applicationModel, report);
    render(applicationGraph, artifactName);
  }
}
