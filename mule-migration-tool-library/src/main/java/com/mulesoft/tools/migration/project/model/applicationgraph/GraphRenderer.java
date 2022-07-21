/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizJdkEngine;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import org.jgrapht.Graph;
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
  private static final AtomicInteger graphFileSuffix = new AtomicInteger();

  static {
    Graphviz.useEngine(new GraphvizJdkEngine());
  }

  public static void render(ApplicationGraph graph, String filePrefix) throws IOException {
    String dot = generateDot(graph.applicationGraph);

    logger.info("\n" + filePrefix + ".dot:\n" + dot);
    MutableGraph g = new Parser().read(dot);
    Graphviz.fromGraph(g).width(1280).render(Format.PNG)
        .toFile(new File(String.format("target/graphs/%s-%s.png", filePrefix, graphFileSuffix.getAndIncrement())));
  }

  private static String generateDot(Graph<FlowComponent, DefaultWeightedEdge> stringGraph)
      throws ExportException {
    DOTExporter<FlowComponent, DefaultWeightedEdge> exporter = new DOTExporter<>(v -> GraphRenderer.getElementName(v));
    exporter.setVertexAttributeProvider((v) -> {
      Map<String, Attribute> map = new LinkedHashMap<>();
      if (stringGraph.inDegreeOf(v) == 0) {
        map.put("style", DefaultAttribute.createAttribute("filled"));
        map.put("fillcolor", DefaultAttribute.createAttribute("cadetblue3"));
      } else if (v instanceof FlowRef) {
        map.put("style", DefaultAttribute.createAttribute("filled"));
        map.put("fillcolor", DefaultAttribute.createAttribute("sandybrown"));
      }
      map.put("label",
              DefaultAttribute.createAttribute(String.format("%s\n(%s)", v.getClass().getSimpleName(), getElementName(v))));
      return map;
    });
    Writer writer = new StringWriter();
    exporter.exportGraph(stringGraph, writer);
    return writer.toString();
  }

  private static String getElementName(FlowComponent v) {
    return v.getName().replaceAll("-", "_");
  }
}
