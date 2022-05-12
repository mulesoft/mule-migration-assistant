/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizJdkEngine;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.ExportException;
import org.jgrapht.nio.dot.DOTExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

public class GraphRenderer {

  private static final Logger logger = LoggerFactory.getLogger(GraphRenderer.class);

  static {
    Graphviz.useEngine(new GraphvizJdkEngine());
  }

  public static void render(ApplicationGraph graph, String filePrefix) throws IOException {
    String dot = generateDot(graph.applicationGraph);
    logger.info("\n" + filePrefix + ".dot:\n" + dot);
    MutableGraph g = new Parser().read(dot);
    Graphviz.fromGraph(g).width(1280).render(Format.PNG).toFile(new File("target/graphs/" + filePrefix + ".png"));
  }

  private static String generateDot(Graph<FlowComponent, DefaultEdge> stringGraph)
      throws ExportException {

    DOTExporter<FlowComponent, DefaultEdge> exporter = new DOTExporter<>(GraphRenderer::getElementName);
    exporter.setVertexAttributeProvider((v) -> {
      Map<String, Attribute> map = new LinkedHashMap<>();
      map.put("label",
              DefaultAttribute.createAttribute(String.format("%s\n(%s)", v.getClass().getSimpleName(), getElementName(v))));
      return map;
    });
    Writer writer = new StringWriter();
    exporter.exportGraph(stringGraph, writer);
    return writer.toString();
  }

  private static String getElementName(FlowComponent v) {
    return (v.getXmlElement().getName() + "__" + v.getParentFlow().getName()).replaceAll("-", "_");
  }
}
