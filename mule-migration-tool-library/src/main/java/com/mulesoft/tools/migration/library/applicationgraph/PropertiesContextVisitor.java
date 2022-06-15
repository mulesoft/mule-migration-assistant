/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.applicationgraph;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mulesoft.tools.migration.project.model.applicationgraph.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Visitor that populates properties context in a flow component
 *
 * @author Mulesoft Inc.
 * @since 1.4.0
 */
public class PropertiesContextVisitor implements FlowComponentVisitor {

  private final List<FlowComponent> previousComponents;
  private final PropertyTranslator inboundTranslator;
  List<PropertiesSourceComponent> componentsWithResponse = Lists.newArrayList();
  PropertiesSourceComponent startingPropertiesSource;

  public PropertiesContextVisitor(List<FlowComponent> previousComponents, PropertyTranslator inboundTranslator,
                                  PropertiesSourceComponent startingPropertiesSource) {
    this.previousComponents = previousComponents;
    this.inboundTranslator = inboundTranslator;
    this.startingPropertiesSource = startingPropertiesSource;
  }

  @Override
  public void visitMessageProcessor(MessageProcessor processor) {
    if (processor.getPropertiesMigrationContext() == null) {
      processor.setPropertiesMigrationContext(new PropertiesMigrationContext(inboundTranslator));
    }
    if (previousComponents != null && !previousComponents.isEmpty()) {
      if (previousComponents.size() > 1) {
        mergeContexts(processor);
      } else {
        FlowComponent singlePreviousComponent = Iterables.getOnlyElement(previousComponents);
        if (singlePreviousComponent instanceof PropertiesSource) {
          processor.getPropertiesMigrationContext()
              .addOriginatingSources(Sets.newHashSet(((PropertiesSource) singlePreviousComponent)));
        } else {
          Set<PropertiesSource> sourceTypesToPropagate =
              singlePreviousComponent.getPropertiesMigrationContext().getOriginatingSources();
          if (singlePreviousComponent.getPropertiesMigrationContext().getOriginatingSources().size() > 1
              && processor.getParentFlow().equals(startingPropertiesSource.getParentFlow())) {
            sourceTypesToPropagate = Sets.newHashSet((startingPropertiesSource));
          }
          processor.getPropertiesMigrationContext().addFromExisting(singlePreviousComponent.getPropertiesMigrationContext(),
                                                                    sourceTypesToPropagate, false);
        }
      }
    }

    clearRemoveNextProperties(processor.getPropertiesMigrationContext());
  }

  private void mergeContexts(MessageProcessor processor) {
    previousComponents.stream().forEach(comp -> {
      if (comp.getPropertiesMigrationContext() != null) {
        if (processor.getPropertiesMigrationContext() == null) {
          processor.setPropertiesMigrationContext(PropertiesMigrationContext.fromContext(comp.getPropertiesMigrationContext()));
        } else {
          processor.getPropertiesMigrationContext().addFromExisting(comp.getPropertiesMigrationContext(), true);
        }
      }
    });
  }

  private void clearRemoveNextProperties(PropertiesMigrationContext propertiesMigrationContext) {
    if (propertiesMigrationContext != null) {
      propertiesMigrationContext.cleanOutbound();
    }
  }

  @Override
  public void visitSetPropertyProcessor(SetPropertyProcessor processor) {
    this.visitMessageProcessor(processor);
    PropertiesMigrationContext existingMigrationContext = processor.getPropertiesMigrationContext();
    existingMigrationContext.addToOutbound(processor.getPropertyName(),
                                           new PropertyMigrationContext(processor.getPropertyTranslation()));
  }

  @Override
  public void visitRemovePropertyProcessor(RemovePropertyProcessor processor) {
    this.visitMessageProcessor(processor);
    PropertiesMigrationContext existingMigrationContext = processor.getPropertiesMigrationContext();

    Map<PropertiesSource, List<String>> keysToRemove = existingMigrationContext.getAllOutboundKeys().entrySet().stream()
        .collect(Collectors.toMap(
                                  e -> e.getKey(),
                                  e -> e.getValue().stream()
                                      .filter(key -> key.matches(processor.getExpression().pattern()))
                                      .collect(Collectors.toList())));

    keysToRemove.entrySet().forEach(keyEntry -> keyEntry.getValue()
        .forEach(key -> processor.getPropertiesMigrationContext().markAsRemoveNext(keyEntry.getKey(), key)));
  }

  @Override
  public void visitCopyPropertiesProcessor(CopyPropertiesProcessor processor) {
    this.visitMessageProcessor(processor);
    PropertiesMigrationContext existingMigrationContext = processor.getPropertiesMigrationContext();
    Map<PropertiesSource, Map<String, String>> inboundContext = existingMigrationContext.getAllInboundTranslations();

    Pattern expression = processor.getExpression();
    inboundContext.entrySet().forEach(inbound -> {
      inbound.getValue().entrySet().stream()
          .filter(e -> expression.matcher(e.getKey()).matches())
          .forEach(e -> existingMigrationContext.addToOutbound(inbound.getKey(), e.getKey(),
                                                               new PropertyMigrationContext(processor
                                                                   .getPropertyTranslation(e.getKey()))));
    });
  }

  @Override
  public void visitPropertiesSourceComponent(PropertiesSourceComponent processor, boolean responseComponent) {
    if (responseComponent) {
      Optional.ofNullable(processor.getResponseComponent())
          .ifPresent(c -> this.visitMessageProcessor(c));

      Optional.ofNullable(processor.getErrorResponseComponent())
          .ifPresent(c -> this.visitMessageProcessor(c));

    } else {
      if (processor.getResponseComponent() != null) {
        componentsWithResponse.add(processor);
      }

      if (processor.getErrorResponseComponent() != null) {
        componentsWithResponse.add(processor);
      }

      // having multiple components converge in a source is not an expected case
      if (!previousComponents.isEmpty()) {
        if (previousComponents.size() > 1) {
          mergeContexts(processor);
        } else {
          FlowComponent previousComponent = Iterables.getOnlyElement(previousComponents);
          processor.setPropertiesMigrationContext(PropertiesMigrationContext
              .fromContext(previousComponent.getPropertiesMigrationContext()));
        }
      } else {
        processor.setPropertiesMigrationContext(new PropertiesMigrationContext(this.inboundTranslator));
      }
    }
  }

  public List<PropertiesSourceComponent> getComponentsWithResponse() {
    return this.componentsWithResponse;
  }

}
