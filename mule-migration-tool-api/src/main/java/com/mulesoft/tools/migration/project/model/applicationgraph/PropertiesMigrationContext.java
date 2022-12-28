/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import static com.mulesoft.tools.migration.project.model.applicationgraph.PropertyTranslator.outboundVariableExpression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Models the context for properties migration
 *
 * @author Mulesoft Inc.
 */
public class PropertiesMigrationContext {

  private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesMigrationContext.class);

  private final PropertyTranslator inboundTranslator;
  private final Set<SourceType> sourceTypes = new TreeSet<>();

  public PropertiesMigrationContext(PropertyTranslator inboundTranslator) {
    this(inboundTranslator, null);
  }

  public PropertiesMigrationContext(PropertyTranslator inboundTranslator, SourceType sourceType) {
    this.inboundTranslator = inboundTranslator;
    if (sourceType != null) {
      this.sourceTypes.add(sourceType);
    }
  }

  public static PropertiesMigrationContext mergeContexts(PropertyTranslator inboundTranslator,
                                                         Deque<FlowComponent> flowComponents) {
    return flowComponents.stream().map(FlowComponent::getPropertiesMigrationContext)
        .reduce(new PropertiesMigrationContext(inboundTranslator),
                (a, b) -> {
                  a.sourceTypes.addAll(b.sourceTypes);
                  return a;
                });
  }

  public List<String> getInboundTranslation(String key) {
    // explicit resolution from current sources
    List<String> result = sourceTypes.stream()
        .filter(s -> inboundTranslator.getAllTranslationsFor(s).containsKey(key))
        .map(s -> inboundTranslator.getAllTranslationsFor(s).get(key))
        .collect(Collectors.toList());

    // explicit resolution from all sources in the application
    if (result.isEmpty()) {
      result = inboundTranslator.getTranslationsForApplicationsSourceTypes().values().stream()
          .filter(m -> m.containsKey(key))
          .map(m -> m.get(key))
          .collect(Collectors.toList());
    }

    // implicit resolution from current sources
    if (result.isEmpty()) {
      result = new ArrayList<>(inboundTranslator.translateImplicit(key, sourceTypes).values());
    }

    return result;
  }

  public boolean hasSingleSourceType() {
    if (sourceTypes.isEmpty()) {
      LOGGER.warn("No source types set");
    }
    return sourceTypes.size() == 1;
  }

  public List<String> getOutboundTranslation(String key) {
    return Collections.singletonList(outboundVariableExpression(key));
  }

  @Override
  public String toString() {
    return sourceTypes.toString();
  }
}
