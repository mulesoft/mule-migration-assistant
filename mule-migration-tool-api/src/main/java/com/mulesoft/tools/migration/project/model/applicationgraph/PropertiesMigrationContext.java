/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.mulesoft.tools.migration.project.model.applicationgraph.SetPropertyProcessor.OUTBOUND_PREFIX;

/**
 * Models the context for properties migration
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public class PropertiesMigrationContext {

  private static final Logger logger = LoggerFactory.getLogger(PropertiesMigrationContext.class);

  private final Map<String, PropertyMigrationContext> inboundContext;
  private final Map<String, PropertyMigrationContext> outboundContext;
  private final SourceType originatingSource;

  public PropertiesMigrationContext(Map<String, PropertyMigrationContext> inboundContext,
                                    Map<String, PropertyMigrationContext> outboundContext,
                                    SourceType originatingSource) {
    this.inboundContext = ImmutableMap.copyOf(inboundContext);
    this.outboundContext = ImmutableMap.copyOf(outboundContext);
    this.originatingSource = originatingSource;
  }

  public Map<String, PropertyMigrationContext> getInboundContext() {
    return this.inboundContext;
  }

  public Map<String, PropertyMigrationContext> getOutboundContext() {
    return this.outboundContext;
  }

  public SourceType getOriginatingSource() {
    return originatingSource;
  }

  public String getInboundTranslation(String key, PropertyTranslator translator, boolean useFallback) {
    return getPropertyTranslation(key, translator, inboundContext,
                                  p -> translator.getTranslationsForApplicationsSourceTypes().get(p), useFallback);
  }

  public String getOutboundTranslation(String key, boolean useFallback) {
    return getPropertyTranslation(key, null, outboundContext, p -> "vars." + OUTBOUND_PREFIX + p, useFallback);
  }

  private String getPropertyTranslation(String key, PropertyTranslator translator,
                                        Map<String, PropertyMigrationContext> migrationContext,
                                        Function<String, String> fallbackTranslationFunction, boolean useFallback) {
    return Optional.ofNullable(migrationContext)
        .map(context -> context.get(key))
        .map(propertyContext -> propertyContext.getTranslation())
        .orElseGet(() -> Optional.ofNullable(tryImplicitTranslation(key, translator))
            .orElseGet(() -> {
              if (useFallback) {
                return tryFallBackTranslation(key, fallbackTranslationFunction);
              } else {
                return null;
              }
            }));
  }

  private String tryImplicitTranslation(String key, PropertyTranslator translator) {
    if (translator != null) {
      return translator.translateImplicit(key, originatingSource);
    }
    return null;
  }

  private String tryFallBackTranslation(String key, Function<String, String> fallbackTranslationFunction) {
    String propertyTranslation = fallbackTranslationFunction.apply(key);
    logger.info("Property '{}' not found in context, using fallback translation '{}' ", key,
                propertyTranslation);
    return propertyTranslation;
  }
}
