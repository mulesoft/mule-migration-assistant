/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.applicationgraph;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mulesoft.tools.migration.project.model.applicationgraph.*;

import java.util.List;
import java.util.Set;


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
    WeightedPathIterator weightedPathIterator = graph.getWeightedPathIterator(start);
    List<FlowComponent> prevComponents;
    Set<PropertiesSourceComponent> componentsWithResponse = Sets.newHashSet();
    List<FlowComponent> leafElelements = Lists.newArrayList();
    PropertiesSourceComponent currentActivePropertySource =
        start instanceof PropertiesSource ? (PropertiesSourceComponent) start : null;
    while (weightedPathIterator.hasNext()) {
      FlowComponent currentComponent = weightedPathIterator.next();
      if (currentComponent instanceof PropertiesSource) {
        currentActivePropertySource = (PropertiesSourceComponent) currentComponent;
      }

      prevComponents = graph.getAllIncomingNodes(currentComponent);

      if (graph.isLeafComponent(currentComponent)) {
        leafElelements.add(currentComponent);
      }
      PropertiesContextVisitor propertiesContextVisitor =
          new PropertiesContextVisitor(prevComponents, graph.getInboundTranslator(), currentActivePropertySource);
      currentComponent.accept(propertiesContextVisitor);
      componentsWithResponse.addAll(propertiesContextVisitor.getComponentsWithResponse());
    }

    final PropertiesSourceComponent lastPropertiesSource = currentActivePropertySource;
    componentsWithResponse.forEach(component -> {
      PropertiesContextVisitor propertiesContextVisitor =
          new PropertiesContextVisitor(leafElelements, graph.getInboundTranslator(), lastPropertiesSource);
      propertiesContextVisitor.visitPropertiesSourceComponent(component, true);
    });
  }


}
