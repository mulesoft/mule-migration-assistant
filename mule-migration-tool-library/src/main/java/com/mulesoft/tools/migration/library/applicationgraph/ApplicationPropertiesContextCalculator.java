/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.applicationgraph;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mulesoft.tools.migration.library.mule.steps.nocompatibility.InboundToAttributesTranslator;
import com.mulesoft.tools.migration.project.model.applicationgraph.*;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Calculates the properties context for a given graph and starting point
 *
 * @author Mulesoft Inc.
 * @since 1.4.0
 */
public class ApplicationPropertiesContextCalculator {

  private final InboundToAttributesTranslator inboundTranslator;

  public ApplicationPropertiesContextCalculator() {
    this.inboundTranslator = new InboundToAttributesTranslator();
  }

  public void calculatePropertiesContext(ApplicationGraph graph) {
    List<FlowComponent> startingPoints = graph.getAllStartingFlowComponents();
    startingPoints.forEach(fc -> calculatePropertiesContext(graph, fc));
  }

  private void calculatePropertiesContext(ApplicationGraph graph, FlowComponent start) {
    DepthFirstIterator depthFirstIterator = graph.getDepthFirstIterator(start);
    FlowComponent prevComponent = start;
    List<PropertiesSourceComponent> componentsWithResponse = Lists.newArrayList();
    while (depthFirstIterator.hasNext()) {
      FlowComponent currentComponent = (FlowComponent) depthFirstIterator.next();
      Map<String, PropertyMigrationContext> inboundPropContext = Maps.newHashMap();

      // TODO: populate outbound context
      Map<String, PropertyMigrationContext> outbundPropContext = Maps.newHashMap();
      if (!(currentComponent instanceof PropertiesSource)) {
        if (prevComponent instanceof PropertiesSource) {
          try {
            inboundPropContext = createSourceGeneratedContext((PropertiesSource) prevComponent);
          } catch (Exception e) {
            // TODO: handleException
          }
          inboundPropContext.putAll(prevComponent.getPropertiesMigrationContext().getInboundContext());
        } else {
          inboundPropContext = prevComponent.getPropertiesMigrationContext().getInboundContext();
        }
      } else if (currentComponent instanceof PropertiesSourceComponent
          && ((PropertiesSourceComponent) currentComponent).getResponseComponent() != null) {
        componentsWithResponse.add((PropertiesSourceComponent) currentComponent);
      }

      prevComponent = currentComponent;
      currentComponent.setPropertiesMigrationContext(new PropertiesMigrationContext(inboundPropContext, outbundPropContext));
    }

    FlowComponent leafElement = graph.getLastFlowComponent(start.getParentFlow());
    componentsWithResponse.forEach(component -> {
      component.getResponseComponent()
          .setPropertiesMigrationContext(new PropertiesMigrationContext(leafElement.getPropertiesMigrationContext()
              .getInboundContext(), leafElement.getPropertiesMigrationContext().getOutboundContext()));
    });
  }

  private Map<String, PropertyMigrationContext> createSourceGeneratedContext(PropertiesSource source) throws Exception {
    return inboundTranslator.getAllTranslationsFor(source).map(translations -> translations.entrySet().stream()
        .collect(Collectors.toMap(
                                  e -> e.getKey(),
                                  e -> new PropertyMigrationContext(e.getValue(), false))))
        .orElse(Maps.newHashMap());
  }
}
