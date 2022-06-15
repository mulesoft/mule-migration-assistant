/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import com.google.common.collect.*;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.mulesoft.tools.migration.project.model.applicationgraph.SetPropertyProcessor.OUTBOUND_PREFIX;

/**
 * Models the context for properties migration
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public class PropertiesMigrationContext {

  private static final Logger logger = LoggerFactory.getLogger(PropertiesMigrationContext.class);

  private final Set<SourceType> originatingSources;
  // even when flow components can have multiple originating sources if they are in shared subflows/flows, outside 
  // of it they should have a single source from where they are coming
  private final Map<SourceType, Map<String, PropertyMigrationContext>> outboundContext;
  private final PropertyTranslator inboundTranslator;

  public PropertiesMigrationContext(PropertyTranslator inboundTranslator) {
    this.originatingSources = Sets.newHashSet();
    this.outboundContext = Maps.newHashMap();
    this.inboundTranslator = inboundTranslator;
  }

  // Translations

  public List<String> getAllInboundKeys() {
    return originatingSources.stream().map(sourceType -> getInboundTranslationForOriginatingSource(sourceType))
        .map(Map::keySet)
        .flatMap(Collection::stream)
        .distinct()
        .collect(Collectors.toList());
  }

  public Map<SourceType, List<String>> getAllOutboundKeys() {
    return originatingSources.stream().collect(Collectors.toMap(Function.identity(),
                                                                sourceType -> getAllOutboundKeys(sourceType)));
  }

  public List<String> getAllOutboundKeys(SourceType sourceType) {
    return Lists.newArrayList(Optional.ofNullable(outboundContext.get(sourceType)).orElse(Maps.newHashMap()).keySet());
  }

  public Map<SourceType, Map<String, String>> getAllInboundTranslations() {
    return originatingSources.stream()
        .collect(Collectors.toMap(
                                  Function.identity(),
                                  source -> getInboundTranslationForOriginatingSource(source)));
  }

  public Map<SourceType, String> getInboundTranslation(String key, boolean useFallback) {

    Map<SourceType, String> potentialTranslations = getAllInboundTranslations().entrySet().stream()
        .filter(e -> e.getValue().containsKey(key))
        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().get(key)));

    if (potentialTranslations.isEmpty()) {
      potentialTranslations = tryImplicitTranslation(key, inboundTranslator);
      if (potentialTranslations.isEmpty()) {
        if (useFallback) {
          return tryFallBackTranslation(key, (k, sourceType) -> tryFindTranslationInAllSources(inboundTranslator
              .getTranslationsForApplicationsSourceTypes(), key, sourceType));
        } else {
          return Maps.newHashMap();
        }
      }
    }

    return potentialTranslations;
  }

  public Map<SourceType, String> getOutboundTranslation(String key, boolean useFallback) {
    Map<SourceType, String> potentialTranslations = originatingSources.stream()
        .filter(sourceType -> outboundContext.containsKey(sourceType) && outboundContext.get(sourceType).containsKey(key))
        .collect(Collectors.toMap(
                                  Function.identity(),
                                  sourceType -> outboundContext.get(sourceType).get(key).getTranslation()));
    if (potentialTranslations == null || potentialTranslations.isEmpty()) {
      if (useFallback) {
        return tryFallBackTranslation(key, (k, sourceType) -> "vars." + OUTBOUND_PREFIX + k);
      } else {
        return Maps.newHashMap();
      }
    }
    return potentialTranslations;
  }

  // Context manipulation

  public static PropertiesMigrationContext fromContext(PropertiesMigrationContext propertiesMigrationContext) {
    PropertiesMigrationContext context = new PropertiesMigrationContext(propertiesMigrationContext.getInboundTranslator());
    propertiesMigrationContext.getOutboundContext().entrySet().forEach(e -> context.addToOutbound(e.getKey(), e.getValue()));
    context.addOriginatingSources(propertiesMigrationContext.getOriginatingSources());
    return context;
  }

  public void addToOutbound(SourceType sourceType, String key, PropertyMigrationContext context) {
    this.outboundContext.computeIfAbsent(sourceType, s -> {
      Map<String, PropertyMigrationContext> contextMap = Maps.newHashMap();
      contextMap.put(key, context);
      return contextMap;
    }).put(key, context);
  }

  public void addToOutbound(String key, PropertyMigrationContext context) {
    this.originatingSources.forEach(s -> addToOutbound(s, key, context));
  }

  public void removeFromOutbound(SourceType sourceType, String key) {
    Optional.ofNullable(this.outboundContext.get(sourceType))
        .ifPresent(contextMap -> contextMap.remove(key));
  }

  public void markAsRemoveNext(SourceType sourceType, String key) {
    Optional.ofNullable(this.outboundContext.get(sourceType)).ifPresent(contextMap -> {
      contextMap.get(key).setRemoveNext();
    });
  }

  public void cleanOutbound() {
    this.originatingSources.stream().forEach(sourceType -> {
      List<String> keysToRemove = Lists.newArrayList();
      Optional.ofNullable(this.outboundContext.get(sourceType))
          .ifPresent(contextMap -> contextMap.entrySet()
              .forEach(contextEntry -> {
                if (contextMap.get(contextEntry.getKey()).isRemoveNext()) {
                  keysToRemove.add(contextEntry.getKey());
                }
              }));
      keysToRemove.forEach(k -> removeFromOutbound(sourceType, k));
    });
  }

  public Set<SourceType> getOriginatingSources() {
    return originatingSources;
  }

  public void addOriginatingSources(Set<SourceType> originatingSources) {
    this.originatingSources.addAll(originatingSources);
  }

  public void addFromExisting(PropertiesMigrationContext propertiesMigrationContext, Set<SourceType> type, boolean optional) {
    if (propertiesMigrationContext != null) {
      this.addOriginatingSources(Sets.newHashSet(type));
      this.getOriginatingSources().forEach(sourceType -> {
        Map<String, PropertyMigrationContext> propertyMigrationContextToAdd =
            propertiesMigrationContext.getOutboundContext(sourceType).entrySet().stream()
                .collect(Collectors.toMap(
                                          entry -> entry.getKey(),
                                          entry -> new PropertyMigrationContext(entry.getValue().getRawTranslation(), optional,
                                                                                entry.getValue().isRemoveNext())));
        Map<String, PropertyMigrationContext> outboundContextForSource = this.outboundContext.get(sourceType);
        if (outboundContextForSource != null) {
          propertyMigrationContextToAdd.forEach(
                                                (key, value) -> outboundContextForSource.merge(key, value, (v1, v2) -> v1));
        } else {
          this.addToOutbound(sourceType, propertyMigrationContextToAdd);
        }
      });
    }
  }

  public void addFromExisting(PropertiesMigrationContext propertiesMigrationContext, boolean optional) {
    this.addFromExisting(propertiesMigrationContext, propertiesMigrationContext.getOriginatingSources(), optional);
  }

  private Map<String, PropertyMigrationContext> getOutboundContext(SourceType sourceType) {
    if (outboundContext.containsKey(sourceType)) {
      return ImmutableMap.copyOf(outboundContext.get(sourceType));
    } else {
      return ImmutableMap.of();
    }
  }

  private Map<SourceType, Map<String, PropertyMigrationContext>> getOutboundContext() {
    return ImmutableMap.copyOf(originatingSources.stream().filter(s -> outboundContext.containsKey(s))
        .collect(Collectors.toMap(Function.identity(), this::getOutboundContext)));
  }

  private void addToOutbound(SourceType sourceType, Map<String, PropertyMigrationContext> contextMap) {
    this.outboundContext.put(sourceType, contextMap);
  }

  private Map<String, String> getInboundTranslationForOriginatingSource(SourceType type) {
    try {
      return inboundTranslator.getAllTranslationsFor(type).orElse(Maps.newHashMap());
    } catch (Exception e) {
    }
    return Maps.newHashMap();
  }

  private Map<SourceType, String> tryImplicitTranslation(String key, PropertyTranslator translator) {
    if (translator != null) {
      return translator.translateImplicit(key, originatingSources);
    }
    return Maps.newHashMap();
  }

  private String tryFindTranslationInAllSources(Map<SourceType, Map<String, String>> allTranslations, String key,
                                                SourceType source) {
    if (allTranslations.get(source).containsKey(key)) {
      return allTranslations.get(source).get(key);
    } else {
      return allTranslations.entrySet().stream()
          .filter(e -> e.getValue().containsKey(key))
          .findFirst().map(e -> e.getValue().get(key)).orElse(null);
    }
  }

  private Map<SourceType, String> tryFallBackTranslation(String key,
                                                         BiFunction<String, SourceType, String> fallbackTranslationFunction) {
    return originatingSources.stream()
        .collect(Collectors.toMap(
                                  Function.identity(),
                                  source -> {
                                    String propertyTranslation = fallbackTranslationFunction.apply(key, source);
                                    logger.info("Property '{}' not found in context, using fallback translation '{}' ", key,
                                                propertyTranslation);
                                    return propertyTranslation;
                                  }));
  }

  PropertyTranslator getInboundTranslator() {
    return this.inboundTranslator;
  }
}
