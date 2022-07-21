/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import static com.mulesoft.tools.migration.project.model.applicationgraph.PropertyTranslator.outboundVariableExpression;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Models the context for properties migration
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public class PropertiesMigrationContext {

  private static final Logger logger = LoggerFactory.getLogger(PropertiesMigrationContext.class);

  private final Set<PropertiesSource> originatingSources;
  // even when flow components can have multiple originating sources if they are in shared subflows/flows, outside 
  // of it they should have a single source from where they are coming
  private final Map<PropertiesSource, Map<String, PropertyMigrationContext>> outboundContext;
  private final PropertyTranslator inboundTranslator;

  public PropertiesMigrationContext(PropertyTranslator inboundTranslator) {
    this.originatingSources = Sets.newHashSet();
    this.outboundContext = Maps.newHashMap();
    this.inboundTranslator = inboundTranslator;
  }

  // Translations

  public List<String> getAllInboundKeys() {
    return originatingSources.stream().map(source -> getInboundTranslationForOriginatingSource(source.getType()))
        .map(Map::keySet)
        .flatMap(Collection::stream)
        .distinct()
        .collect(Collectors.toList());
  }

  public Map<PropertiesSource, List<String>> getAllOutboundKeys() {
    return originatingSources.stream().collect(Collectors.toMap(Function.identity(),
                                                                source -> getAllOutboundKeys(source)));
  }

  private List<String> getAllOutboundKeys(PropertiesSource source) {
    return Lists.newArrayList(Optional.ofNullable(outboundContext.get(source)).orElse(Maps.newHashMap()).keySet());
  }

  public Map<PropertiesSource, Map<String, String>> getAllInboundTranslations() {
    return originatingSources.stream()
        .collect(Collectors.toMap(
                                  Function.identity(),
                                  source -> getInboundTranslationForOriginatingSource(source.getType())));
  }

  public List<String> getInboundTranslation(String key, boolean useFallback) {
    Map<PropertiesSource, String> potentialTranslations = getAllInboundTranslations().entrySet().stream()
        .filter(e -> e.getValue().containsKey(key))
        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().get(key)));

    if (potentialTranslations.isEmpty()) {
      potentialTranslations = tryImplicitTranslation(key, inboundTranslator);
      if (potentialTranslations.isEmpty()) {
        if (useFallback) {
          potentialTranslations = tryFallBackTranslation(key, (k, sourceType) -> tryFindTranslationInAllSources(inboundTranslator
              .getTranslationsForApplicationsSourceTypes(), key, sourceType));
        } else {
          potentialTranslations = Maps.newHashMap();
        }
      }
    }

    return potentialTranslations.values().stream().sorted().distinct().collect(Collectors.toList());
  }

  public List<String> getOutboundTranslation(String key, boolean useFallback) {
    Map<PropertiesSource, String> potentialTranslations = originatingSources.stream()
        .filter(source -> outboundContext.containsKey(source) && outboundContext.get(source).containsKey(key))
        .collect(Collectors.toMap(
                                  Function.identity(),
                                  source -> outboundContext.get(source).get(key).getTranslation()));
    if (potentialTranslations == null || potentialTranslations.isEmpty()) {
      if (useFallback) {
        potentialTranslations = tryFallBackTranslation(key, (k, sourceType) -> outboundVariableExpression(k));
      } else {
        potentialTranslations = Maps.newHashMap();
      }
    }
    return potentialTranslations.values().stream().sorted().distinct().collect(Collectors.toList());
  }

  // Context manipulation

  public static PropertiesMigrationContext fromContext(PropertiesMigrationContext propertiesMigrationContext) {
    PropertiesMigrationContext context = new PropertiesMigrationContext(propertiesMigrationContext.getInboundTranslator());
    propertiesMigrationContext.getOutboundContext().entrySet().forEach(e -> context.addToOutbound(e.getKey(), e.getValue()));
    context.addOriginatingSources(propertiesMigrationContext.getOriginatingSources());
    return context;
  }

  public void addToOutbound(PropertiesSource source, String key, PropertyMigrationContext context) {
    this.outboundContext.computeIfAbsent(source, s -> {
      Map<String, PropertyMigrationContext> contextMap = Maps.newHashMap();
      contextMap.put(key, context);
      return contextMap;
    }).put(key, context);
  }

  public void addToOutbound(String key, PropertyMigrationContext context) {
    this.originatingSources.forEach(s -> addToOutbound(s, key, context));
  }

  public void removeFromOutbound(PropertiesSource source, String key) {
    Optional.ofNullable(this.outboundContext.get(source))
        .ifPresent(contextMap -> contextMap.remove(key));
  }

  public void markAsRemoveNext(PropertiesSource source, String key) {
    Optional.ofNullable(this.outboundContext.get(source)).ifPresent(contextMap -> {
      contextMap.get(key).setRemoveNext();
    });
  }

  public void cleanOutbound() {
    this.originatingSources.stream().forEach(source -> {
      List<String> keysToRemove = Lists.newArrayList();
      Optional.ofNullable(this.outboundContext.get(source))
          .ifPresent(contextMap -> contextMap.entrySet()
              .forEach(contextEntry -> {
                if (contextMap.get(contextEntry.getKey()).isRemoveNext()) {
                  keysToRemove.add(contextEntry.getKey());
                }
              }));
      keysToRemove.forEach(k -> removeFromOutbound(source, k));
    });
  }

  public Set<PropertiesSource> getOriginatingSources() {
    return originatingSources;
  }

  public void addOriginatingSources(Set<PropertiesSource> originatingSources) {
    this.originatingSources.addAll(originatingSources);
  }

  public void addFromExisting(PropertiesMigrationContext propertiesMigrationContext, Set<PropertiesSource> sources,
                              boolean optional) {
    if (propertiesMigrationContext != null) {
      this.addOriginatingSources(Sets.newHashSet(sources));
      this.getOriginatingSources().forEach(source -> {
        Map<String, PropertyMigrationContext> propertyMigrationContextToAdd =
            propertiesMigrationContext.getOutboundContext(source).entrySet().stream()
                .collect(Collectors.toMap(
                                          entry -> entry.getKey(),
                                          entry -> new PropertyMigrationContext(entry.getValue().getRawTranslation(), optional,
                                                                                entry.getValue().isRemoveNext())));
        Map<String, PropertyMigrationContext> outboundContextForSource = this.outboundContext.get(source);
        if (outboundContextForSource != null) {
          propertyMigrationContextToAdd.forEach(
                                                (key, value) -> outboundContextForSource.merge(key, value, (v1, v2) -> v1));
        } else {
          this.addToOutbound(source, propertyMigrationContextToAdd);
        }
      });
    }
  }

  public void addFromExisting(PropertiesMigrationContext propertiesMigrationContext, boolean optional) {
    this.addFromExisting(propertiesMigrationContext, propertiesMigrationContext.getOriginatingSources(), optional);
  }

  private Map<String, PropertyMigrationContext> getOutboundContext(PropertiesSource source) {
    if (outboundContext.containsKey(source)) {
      return ImmutableMap.copyOf(outboundContext.get(source));
    } else {
      return ImmutableMap.of();
    }
  }

  private Map<PropertiesSource, Map<String, PropertyMigrationContext>> getOutboundContext() {
    return ImmutableMap.copyOf(originatingSources.stream().filter(s -> outboundContext.containsKey(s))
        .collect(Collectors.toMap(Function.identity(), this::getOutboundContext)));
  }

  private void addToOutbound(PropertiesSource sourceType, Map<String, PropertyMigrationContext> contextMap) {
    this.outboundContext.put(sourceType, contextMap);
  }

  private Map<String, String> getInboundTranslationForOriginatingSource(SourceType type) {
    try {
      return inboundTranslator.getAllTranslationsFor(type).orElse(Maps.newHashMap());
    } catch (Exception e) {
    }
    return Maps.newHashMap();
  }

  private Map<PropertiesSource, String> tryImplicitTranslation(String key, PropertyTranslator translator) {
    if (translator != null) {
      return originatingSources.stream().filter(source -> source.getType().supportsImplicit())
          .collect(Collectors.toMap(Function.identity(), s -> translator.translateImplicit(key, s.getType())));
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

  private Map<PropertiesSource, String> tryFallBackTranslation(String key,
                                                               BiFunction<String, SourceType, String> fallbackTranslationFunction) {
    return originatingSources.stream().filter(source -> fallbackTranslationFunction.apply(key, source.getType()) != null)
        .collect(Collectors.toMap(
                                  Function.identity(),
                                  source -> {
                                    String propertyTranslation = fallbackTranslationFunction.apply(key, source.getType());
                                    logger.info("Property '{}' not found in context, using fallback translation '{}' ", key,
                                                propertyTranslation);
                                    return propertyTranslation;
                                  }));
  }

  PropertyTranslator getInboundTranslator() {
    return this.inboundTranslator;
  }
}
