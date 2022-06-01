/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.applicationgraph;

import com.google.common.collect.Lists;
import com.mulesoft.tools.migration.project.model.applicationgraph.*;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.List;


/**
 * Calculates the properties context for a given graph and starting point
 *
 * @author Mulesoft Inc.
 * @since 1.4.0
 */
public class ApplicationPropertiesContextCalculator {

  public void calculatePropertiesContext(ApplicationGraph graph) {
    List<FlowComponent> startingPoints = graph.getAllStartingFlowComponents();
    startingPoints.forEach(fc -> calculatePropertiesContext(graph, fc));
  }

  private void calculatePropertiesContext(ApplicationGraph graph, FlowComponent start) {
    DepthFirstIterator depthFirstIterator = graph.getDepthFirstIterator(start);
    FlowComponent prevComponent = null;
    List<PropertiesSourceComponent> componentsWithResponse = Lists.newArrayList();
    while (depthFirstIterator.hasNext()) {
      FlowComponent currentComponent = (FlowComponent) depthFirstIterator.next();
      PropertiesContextVisitor propertiesContextVisitor = new PropertiesContextVisitor(prevComponent);
      currentComponent.accept(propertiesContextVisitor);
      componentsWithResponse.addAll(propertiesContextVisitor.getComponentsWithResponse());
      prevComponent = currentComponent;
    }

    FlowComponent leafElement = graph.getLastFlowComponent(start.getParentFlow());
    componentsWithResponse.forEach(component -> {
      PropertiesContextVisitor propertiesContextVisitor = new PropertiesContextVisitor(leafElement);
      propertiesContextVisitor.visitPropertiesSourceComponent(component, true);
    });
  }


}
