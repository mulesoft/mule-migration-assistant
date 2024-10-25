/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.MIGRATION_ID_ATTRIBUTE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.MIGRATION_NAMESPACE;
import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Optional;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Models a generic message processor
 *
 * @author Mulesoft Inc.
 */
public class BasicFlowComponent implements FlowComponent {

  private static final Logger LOGGER = LoggerFactory.getLogger(BasicFlowComponent.class);

  protected final Deque<FlowComponent> next = new ArrayDeque<>();
  protected final Deque<FlowComponent> previous = new ArrayDeque<>();

  protected final Element element;
  protected final Flow flow;
  protected final ApplicationGraph applicationGraph;
  protected PropertiesMigrationContext inputContext;
  protected PropertiesMigrationContext outputContext;
  protected FlowRefFlowComponent flowRefCaller;

  public BasicFlowComponent(Element xmlElement, Flow parentFLow, ApplicationGraph applicationGraph) {
    requireNonNull(xmlElement);
    requireNonNull(parentFLow);
    requireNonNull(applicationGraph);

    this.element = xmlElement;
    this.flow = parentFLow;
    this.applicationGraph = applicationGraph;
    this.inputContext = new PropertiesMigrationContext(applicationGraph.getInboundTranslator());
  }

  @Override
  public String getElementId() {
    return element.getAttributeValue(MIGRATION_ID_ATTRIBUTE, MIGRATION_NAMESPACE);
  }

  @Override
  public Flow getParentFlow() {
    return flow;
  }

  @Override
  public Element getXmlElement() {
    return element;
  }

  @Override
  public PropertiesMigrationContext getPropertiesMigrationContext() {
    return getInputContext();
  }

  public PropertiesMigrationContext getInputContext() {
    return inputContext;
  }

  public PropertiesMigrationContext getOutputContext() {
    return outputContext;
  }

  @Override
  public String getName() {
    String ns = element.getNamespace().getPrefix();
    return String.format("%s%s//%s//%s", ns.isEmpty() ? "" : ns + ":",
                         element.getName(), flow.getName(), getElementId().substring(0, 4));
  }

  @Override
  public void next(FlowComponent nextComponent) {
    next.addFirst(nextComponent);
    ((BasicFlowComponent) nextComponent).previous.addFirst(this);
  }

  @Override
  public void resetNext(FlowComponent nextComponent) {
    new ArrayList<>(next()).forEach(fc -> {
      next().remove(fc);
      fc.previous().remove(this);
    });
    next(nextComponent);
  }

  @Override
  public Deque<FlowComponent> next() {
    return next;
  }

  @Override
  public Deque<FlowComponent> previous() {
    return previous;
  }

  @Override
  public FlowComponent rewire(Deque<Flow> flowStack) {
    updatePropertiesContext();
    return nextComponentToProcess(flowStack);
  }

  public FlowComponent nextComponentToProcess(Deque<Flow> flowStack) {
    if (flowRefCaller != null) {
      flowStack.remove(getParentFlow());
    }
    FlowComponent nextComponent = next.peek();
    if (next.size() > 1) {
      // next picked matching flow stack
      Optional<FlowComponent> any = next.stream().filter(fc -> fc.getParentFlow().equals(flowStack.peekLast())).findAny();
      if (any.isPresent()) {
        nextComponent = any.get();
      } else {
        LOGGER.warn("Could not find next component for {}", this);
      }
    }
    if (nextComponent instanceof DummyFlowTerminalComponent) {
      ((DummyFlowTerminalComponent) nextComponent).updatePropertiesContext();
    }
    if (this instanceof FlowRefFlowComponent && !getParentFlow().equals(nextComponent.getParentFlow())) {
      if (flowStack.contains(getParentFlow())) {
        throw new InvalidGraphStateException("Loop detected");
      }
      flowStack.offer(getParentFlow());
    }
    LOGGER.debug("Current flow stack: {}", flowStack);
    return nextComponent;
  }

  public void updatePropertiesContext() {
    updatePropertiesContext(null);
  }

  public void updatePropertiesContext(PropertiesMigrationContext outputContext) {
    if (previous.isEmpty()) {
      LOGGER.warn("No previous nodes");
      return;
    }
    if (flowRefCaller != null) { // return from flow-ref
      if (previous.peek().getPropertiesMigrationContext().hasSingleSourceType()) {
        this.inputContext = ((BasicFlowComponent) previous.peek()).getOutputContext();
      } else {
        this.inputContext = flowRefCaller.getOutputContext();
      }
    } else if (previous.size() > 1) {
      this.inputContext = PropertiesMigrationContext.mergeContexts(applicationGraph.getInboundTranslator(), previous);
    } else {
      this.inputContext = ((BasicFlowComponent) previous.peek()).getOutputContext();
    }
    if (outputContext != null) {
      this.outputContext = outputContext;
    } else {
      this.outputContext = this.inputContext;
    }
    LOGGER.debug("context for node {} -- inputCtx: {} -- outputCtx: {}", this, this.inputContext, this.outputContext);
  }

  @Override
  public String toString() {
    return getName();
  }
}
