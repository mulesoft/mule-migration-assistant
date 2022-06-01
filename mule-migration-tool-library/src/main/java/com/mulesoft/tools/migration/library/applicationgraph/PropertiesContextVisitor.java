/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.applicationgraph;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mulesoft.tools.migration.library.nocompatibility.InboundToAttributesTranslator;
import com.mulesoft.tools.migration.project.model.applicationgraph.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Visitor that populates properties context in a flow component
 *
 * @author Mulesoft Inc.
 * @since 1.4.0
 */
public class PropertiesContextVisitor implements FlowComponentVisitor {

  private final FlowComponent previousComponent;
  private final InboundToAttributesTranslator inboundTranslator;
  List<PropertiesSourceComponent> componentsWithResponse = Lists.newArrayList();

  public PropertiesContextVisitor(FlowComponent previousComponent) {
    this.previousComponent = previousComponent;
    this.inboundTranslator = new InboundToAttributesTranslator();
  }

  @Override
  public void visitMessageProcessor(MessageProcessor processor) {
    Map<String, PropertyMigrationContext> inboundPropContext = Maps.newHashMap();
    Map<String, PropertyMigrationContext> outbundPropContext = Maps.newHashMap();
    SourceType originatingSource = null;

    clearRemoveNextProperties(processor.getPropertiesMigrationContext());
    if (previousComponent != null) {
      inboundPropContext.putAll(previousComponent.getPropertiesMigrationContext().getInboundContext());
      outbundPropContext = previousComponent.getPropertiesMigrationContext().getOutboundContext();
      if (previousComponent instanceof PropertiesSource) {
        try {
          inboundPropContext.putAll(createSourceGeneratedContext((PropertiesSource) previousComponent));
        } catch (Exception e) {
          // TODO: handleException
        }
        originatingSource = ((PropertiesSource) previousComponent).getType();
      } else {
        originatingSource = previousComponent.getPropertiesMigrationContext().getOriginatingSource();
      }
    }

    processor
        .setPropertiesMigrationContext(new PropertiesMigrationContext(inboundPropContext, outbundPropContext, originatingSource));
  }

  private PropertiesMigrationContext clearRemoveNextProperties(PropertiesMigrationContext propertiesMigrationContext) {
    if (propertiesMigrationContext != null) {
      Map<String, PropertyMigrationContext> outboundContext = propertiesMigrationContext.getOutboundContext().entrySet().stream()
          .filter(e -> !e.getValue().isRemoveNext())
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

      Map<String, PropertyMigrationContext> inboundContext = propertiesMigrationContext.getInboundContext().entrySet().stream()
          .filter(e -> !e.getValue().isRemoveNext())
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      return new PropertiesMigrationContext(inboundContext, outboundContext, propertiesMigrationContext.getOriginatingSource());
    }

    return propertiesMigrationContext;
  }

  @Override
  public void visitSetPropertyProcessor(SetPropertyProcessor processor) {
    this.visitMessageProcessor(processor);
    PropertiesMigrationContext existingMigrationContext = processor.getPropertiesMigrationContext();
    Map outboundContext = Maps.newHashMap(existingMigrationContext.getOutboundContext());
    outboundContext.put(processor.getPropertyName(), new PropertyMigrationContext(processor.getPropertyTranslation()));

    processor.setPropertiesMigrationContext(new PropertiesMigrationContext(existingMigrationContext.getInboundContext(),
                                                                           outboundContext,
                                                                           existingMigrationContext.getOriginatingSource()));
  }

  @Override
  public void visitRemovePropertyProcessor(RemovePropertyProcessor processor) {
    this.visitMessageProcessor(processor);
    PropertiesMigrationContext existingMigrationContext = processor.getPropertiesMigrationContext();
    Map<String, PropertyMigrationContext> outboundContext = Maps.newHashMap(existingMigrationContext.getOutboundContext());

    if (outboundContext.containsKey(processor.getPropertyName())) {

      outboundContext.put(processor.getPropertyName(), outboundContext.get(processor.getPropertyName()).setRemoveNext());
    }

    processor.setPropertiesMigrationContext(new PropertiesMigrationContext(existingMigrationContext.getInboundContext(),
                                                                           outboundContext,
                                                                           existingMigrationContext.getOriginatingSource()));
  }

  @Override
  public void visitPropertiesSourceComponent(PropertiesSourceComponent processor, boolean responseComponent) {
    if (responseComponent) {
      processor.getResponseComponent()
          .setPropertiesMigrationContext(new PropertiesMigrationContext(previousComponent.getPropertiesMigrationContext()
              .getInboundContext(),
                                                                        previousComponent.getPropertiesMigrationContext()
                                                                            .getOutboundContext(),
                                                                        previousComponent.getPropertiesMigrationContext()
                                                                            .getOriginatingSource()));
    } else {
      if (processor.getResponseComponent() != null) {
        componentsWithResponse.add(processor);
      }

      if (previousComponent != null) {
        processor.setPropertiesMigrationContext(new PropertiesMigrationContext(
                                                                               previousComponent.getPropertiesMigrationContext()
                                                                                   .getInboundContext(),
                                                                               previousComponent.getPropertiesMigrationContext()
                                                                                   .getOutboundContext(),
                                                                               previousComponent.getPropertiesMigrationContext()
                                                                                   .getOriginatingSource()));
      } else {
        processor.setPropertiesMigrationContext(new PropertiesMigrationContext(Maps.newHashMap(), Maps.newHashMap(), null));
      }
    }
  }

  public List<PropertiesSourceComponent> getComponentsWithResponse() {
    return this.componentsWithResponse;
  }

  private Map<String, PropertyMigrationContext> createSourceGeneratedContext(PropertiesSource source) throws Exception {
    return inboundTranslator.getAllTranslationsFor(source.getType()).map(translations -> translations.entrySet().stream()
        .collect(Collectors.toMap(
                                  e -> e.getKey(),
                                  e -> new PropertyMigrationContext(e.getValue(), false, false))))
        .orElse(Maps.newHashMap());
  }
}
