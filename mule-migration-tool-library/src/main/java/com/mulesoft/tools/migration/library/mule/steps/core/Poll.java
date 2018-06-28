/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.addChildNode;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;

/**
 * Migration step for poll component
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Poll extends AbstractApplicationModelMigrationStep {

  private static final String CRON_FREQ_SCHEDULER = "cron-scheduler";
  private static final String FIXED_FREQ_SCHEDULER = "fixed-frequency-scheduler";
  private static final String SCHEDULING_STRATEGY = "scheduling-strategy";
  private static final String POLL_NEW_NAME = "scheduler";
  private static final String PROCESSOR_CHAIN = "processor-chain";
  private static final String XPATH_SELECTOR = "//*[local-name()='poll']";
  private static final String SCHEDULERS_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/schedulers";
  private static final String SCHEDULERS_NAMESPACE_PREFIX = "schedulers";
  private static final Namespace SCHEDULERS_NAMESPACE =
      Namespace.getNamespace(SCHEDULERS_NAMESPACE_PREFIX, SCHEDULERS_NAMESPACE_URI);
  private static final String CORE_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/core";
  private static final String CORE_NAME = "mule";
  private static final Namespace CORE_NAMESPACE = Namespace.getNamespace(CORE_NAME, CORE_NAMESPACE_URI);

  @Override
  public String getDescription() {
    return "Update Poll component.";
  }

  public Poll() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(CORE_NAMESPACE, SCHEDULERS_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    try {
      changeNodeName("", POLL_NEW_NAME)
          .apply(element);

      element.setAttribute("isMessageSource", "true");

      List<Element> childElementsToMove = element.getChildren().stream()
          .filter(s -> !StringUtils.equals(s.getName(), FIXED_FREQ_SCHEDULER)
              && !StringUtils.equals(s.getName(), CRON_FREQ_SCHEDULER))
          .collect(Collectors.toList());

      movePollChildsToParent(childElementsToMove, element.getParentElement(), element.getParentElement().indexOf(element) + 1);

      updateCronScheduler(element);
      updateFixedFrequencyScheduler(element);

    } catch (Exception ex) {
      throw new MigrationStepException("Failed to migrate poll.");
    }
  }

  private void updateFixedFrequencyScheduler(Element element) {
    if (element.getChild(FIXED_FREQ_SCHEDULER, CORE_NAMESPACE) != null) {
      Element fixedScheduler = element.getChild(FIXED_FREQ_SCHEDULER, CORE_NAMESPACE);
      moveSchedulerToSchedulingStrategy(fixedScheduler, "fixed-frequency");
      moveAttributeToChildNode(fixedScheduler.getAttribute("frequency"), element, "fixed-frequency");
      moveAttributeToChildNode(fixedScheduler.getAttribute("startDelay"), element, "fixed-frequency");
      moveAttributeToChildNode(fixedScheduler.getAttribute("timeUnit"), element, "fixed-frequency");
    }
  }

  private void updateCronScheduler(Element element) {
    if (element.getChild(CRON_FREQ_SCHEDULER, SCHEDULERS_NAMESPACE) != null) {
      Element cronScheduler = element.getChild(CRON_FREQ_SCHEDULER, SCHEDULERS_NAMESPACE);
      moveSchedulerToSchedulingStrategy(cronScheduler, "cron");
      moveAttributeToChildNode(cronScheduler.getAttribute("expression"), element, "cron");
      moveAttributeToChildNode(cronScheduler.getAttribute("timeZone"), element, "cron");
    }
  }

  private void moveSchedulerToSchedulingStrategy(Element element, String newSchedulerChildNode) {
    addChildNode("", SCHEDULING_STRATEGY).apply(element.getParentElement());
    addChildNode("", newSchedulerChildNode).apply(element.getParentElement().getChild(SCHEDULING_STRATEGY, CORE_NAMESPACE));
    element.getParent().removeContent(element);
  }

  private void moveAttributeToChildNode(Attribute attribute, Element parent, String childName) {
    if (attribute != null) {
      attribute.getParent().removeAttribute(attribute);
      Element scheduler = parent.getChild(SCHEDULING_STRATEGY, CORE_NAMESPACE);
      Element childScheduler = scheduler.getChild(childName, CORE_NAMESPACE);
      childScheduler.setAttribute(attribute);
    }
  }

  private void movePollChildsToParent(List<Element> elements, Element parent, Integer position) {
    List<Element> childs = new ArrayList<>();
    elements.forEach(n -> {
      if (StringUtils.equals(n.getName(), PROCESSOR_CHAIN)) {
        movePollChildsToParent(n.getChildren(), parent, position);
        n.detach();
      } else {
        childs.add(n);
      }
    });
    if (childs.size() > 0) {
      childs.forEach(s -> s.getParent().removeContent(s));
      parent.addContent(position, childs);
    }
  }


}
