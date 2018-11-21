/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.addChildNode;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementToBottom;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addMigrationAttributeToElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getFlow;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.library.mule.steps.os.AbstractOSMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Migration step for poll component
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Poll extends AbstractOSMigrator {

  private static final String CRON_FREQ_SCHEDULER = "cron-scheduler";
  private static final String FIXED_FREQ_SCHEDULER = "fixed-frequency-scheduler";
  private static final String WATERMARK = "watermark";
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

      addMigrationAttributeToElement(element, new Attribute("isMessageSource", "true"));

      List<Element> childElementsToMove = element.getChildren().stream()
          .filter(s -> !StringUtils.equals(s.getName(), FIXED_FREQ_SCHEDULER)
              && !StringUtils.equals(s.getName(), CRON_FREQ_SCHEDULER)
              && !StringUtils.equals(s.getName(), WATERMARK))
          .collect(Collectors.toList());

      movePollChildsToParent(childElementsToMove, element.getParentElement(), element.getParentElement().indexOf(element) + 1);

      if (element.getChild(FIXED_FREQ_SCHEDULER, CORE_NAMESPACE) == null
          && element.getChild(CRON_FREQ_SCHEDULER, SCHEDULERS_NAMESPACE) == null) {
        Element schedulingStrategy = new Element("scheduling-strategy", element.getNamespace());
        schedulingStrategy.addContent(new Element("fixed-frequency", element.getNamespace()));
        element.addContent(schedulingStrategy);
      } else {
        updateCronScheduler(element);
        updateFixedFrequencyScheduler(element);
      }

      if (element.getChild(WATERMARK, CORE_NAMESPACE) != null) {
        Element watermark = element.getChild(WATERMARK, CORE_NAMESPACE);
        Element osStore = new Element("store", NEW_OS_NAMESPACE);
        Element osRetrieve = new Element("retrieve", NEW_OS_NAMESPACE);

        addMigrationAttributeToElement(osStore, new Attribute("lastElement", "true"));

        osStore.setAttribute("failIfPresent", "false");
        osStore.setAttribute("failOnNullValue", "false");

        if (watermark.getAttribute("variable") != null) {
          osStore.setAttribute("key", watermark.getAttributeValue("variable"));
          osRetrieve.setAttribute("key", watermark.getAttributeValue("variable"));
          osRetrieve.setAttribute("target", watermark.getAttributeValue("variable"));
        }

        if (watermark.getAttribute("default-expression") != null) {
          String defaultExpression =
              getExpressionMigrator().migrateExpression(watermark.getAttributeValue("default-expression"), true, element);
          setOSValue(osRetrieve, defaultExpression, "default-value");
        }

        if (watermark.getAttribute("update-expression") != null) {
          String updateExpression =
              getExpressionMigrator().migrateExpression(watermark.getAttributeValue("update-expression"), true, element);
          setOSValue(osStore, updateExpression, "value");
        } else if (watermark.getAttribute("selector-expression") != null || watermark.getAttribute("selector") != null) {
          String selectorExpression = watermark.getAttributeValue("selector-expression");
          String selector = watermark.getAttributeValue("selector");

          if (selectorExpression == null) {
            setOSValue(osStore, getExpressionFromSelector(selector), "value");
          } else if (selector == null) {
            selectorExpression = getExpressionMigrator().migrateExpression(selectorExpression, true, element);
            setOSValue(osStore, selectorExpression, "value");
          } else {
            //TODO After MMT-262 is completed, need to pass both values (selector and selector expression) on the expressions migrator and set that value on the OS processor.
          }
        }

        if (watermark.getAttribute("object-store-ref") != null) {
          osStore.setAttribute("objectStore", watermark.getAttributeValue("object-store-ref"));
          osRetrieve.setAttribute("objectStore", watermark.getAttributeValue("object-store-ref"));
        }

        addElementAfter(osRetrieve, element);
        addElementToBottom(getFlow(element), osStore);

        watermark.detach();
        addOSModule();
      }

    } catch (Exception ex) {
      throw new MigrationStepException("Failed to migrate poll." + ex.getMessage() + ex.getStackTrace());
    }
  }

  private String getExpressionFromSelector(String selector) {
    String expression;
    switch (selector.toLowerCase()) {
      case "min":
        expression = "#[min(payload)]";
        break;
      case "max":
        expression = "#[max(payload)]";
        break;
      case "first":
        expression = "#[payload[0]]";
        break;
      case "last":
        expression = "#[payload[-1]]";
        break;
      default:
        throw new IllegalArgumentException("Selector " + selector + " doesn't match with any valid value.");
    }
    return expression;
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
