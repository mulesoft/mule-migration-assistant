/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.splitter;

import static com.mulesoft.tools.migration.library.mule.steps.splitter.SplitterAggregatorUtils.setAggregatorAsProcessed;
import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.VM_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.migrateVmConfig;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementBefore;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addNewFlowAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getFlow;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Contains all common logic for migrating any '*-splitter' and it's matching aggregator.
 * </p>
 * For every splitter that triggers the execution, the flow will be iterated until a matching aggregator is found or the flow ends.
 * Then every message processor found will be put inside the scope of a 'foreach' element, with an aggregator at the end that will trigger when all the elements were processed.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractSplitter extends AbstractApplicationModelMigrationStep {

  private static final String OLD_AGGREGATOR_TIMEOUT_ATTRIBUTE = "timeout";
  private static final String OLD_AGGREGATOR_FAIL_ON_TIMEOUT_ATTRIBUTE = "failOnTimeout";
  private static final String OLD_AGGREGATOR_PROCESSED_GROUPS_OBJECT_STORE_REF_ATTRIBUTE = "processed-groups-object-store-ref";
  private static final String OLD_AGGREGATOR_EVENT_GROUPS_OBJECT_STORE_REF_ATTRIBUTE = "event-groups-object-store-ref";
  private static final String OLD_AGGREGATOR_PERSISTENT_STORES_ATTRIBUTE = "persistentStores";
  private static final String OLD_AGGREGATOR_STORE_PREFIX_ATTRIBUTE = "storePrefix";

  private static final String AGGREGATORS_NAMESPACE_PREFIX = "aggregators";
  private static final String AGGREGATORS_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/aggregators";
  static final Namespace AGGREGATORS_NAMESPACE = getNamespace(AGGREGATORS_NAMESPACE_PREFIX, AGGREGATORS_NAMESPACE_URI);

  private static final Element SET_VARIABLE_TEMPLATE = new Element("set-variable", CORE_NAMESPACE);

  private static final Element SET_PAYLOAD_TEMPLATE = new Element("set-payload", CORE_NAMESPACE);

  private static final Element AGGREGATOR_TEMPLATE =
      new Element("group-based-aggregator", AGGREGATORS_NAMESPACE).setAttribute("evictionTime", "0");

  private static final Element AGGREGATOR_LISTENER_TEMPLATE = new Element("aggregator-listener", AGGREGATORS_NAMESPACE)
      .setAttribute("includeTimedOutGroups", "true");

  private static final Element VM_QUEUE_TEMPLATE = new Element("queue", VM_NAMESPACE);

  private static final Element FOR_EACH_TEMPLATE_ELEMENT = new Element("foreach", CORE_NAMESPACE);
  private static final String FOR_EACH_COLLECTION_ATTRIBUTE_KEY = "collection";

  private static final Element VM_CONSUME_TEMPLATE_ELEMENT = new Element("consume", VM_NAMESPACE);
  private static final Element VM_PUBLISH_TEMPLATE_ELEMENT = new Element("publish", VM_NAMESPACE);

  private static final Element CHOICE_TEMPLATE_ELEMENT = new Element("choice", CORE_NAMESPACE);
  private static final Element WHEN_TEMPLATE_ELEMENT = new Element("when", CORE_NAMESPACE);

  protected abstract String getMatchingAggregatorName();

  protected Optional<String> getForEachCollectionAttribute(Element splitterElement) {
    return empty();
  }

  @Override
  public void execute(Element splitter, MigrationReport report) throws RuntimeException {
    SplitterAggregatorInfo splitterAggregatorInfo = new SplitterAggregatorInfo(splitter, getApplicationModel());
    List<ReportEntry> reports = new LinkedList<>();
    Element forEachElement = null;

    //Check if enableCorrelation=NEVER and report it
    registerNeverEnableCorrelationReport(splitter, reports);

    List<Element> elementsBetweenSplitterAndAggregator = collectUntilAggregator(splitter);
    Element aggregatorElement = null;
    if (!elementsBetweenSplitterAndAggregator.isEmpty()) {
      if (foundMatchingAggregator(elementsBetweenSplitterAndAggregator.get(elementsBetweenSplitterAndAggregator.size() - 1))) {
        aggregatorElement = elementsBetweenSplitterAndAggregator.remove(elementsBetweenSplitterAndAggregator.size() - 1);

        //Set as processed to be able to decide to report some things
        setAggregatorAsProcessed(aggregatorElement);
      }
    }

    if (!isCustomAggregator(aggregatorElement)) {
      if (aggregatorElement == null) {
        //There is no related aggregator
        registerNoAggregatorReport(splitter, reports);
      } else {
        //Splitter has a matching aggregator
        aggregatorElement.detach();
      }
      Map<String, String> oldAggregatorAttributes = getOldAggregatorAttributes(aggregatorElement);
      forEachElement =
          wrapWithForEachAndAggregator(splitterAggregatorInfo, oldAggregatorAttributes, elementsBetweenSplitterAndAggregator);
      replaceInDocument(splitter, forEachElement, splitterAggregatorInfo, oldAggregatorAttributes);
      reportOldAggregatorAttributes(reports, oldAggregatorAttributes, aggregatorElement,
                                    forEachElement.getChild(AGGREGATOR_TEMPLATE.getName(), AGGREGATORS_NAMESPACE));
    } else {
      //Splitter has a custom aggregator
      reportCustomAggregator(aggregatorElement, report);
    }
    //Write collected reports
    writeRegisteredReports(forEachElement, reports, report);
  }

  private void replaceInDocument(Element splitterElement,
                                 Element forEachAggregatorElement,
                                 SplitterAggregatorInfo splitterAggregatorInfo,
                                 Map<String, String> oldAggregatorAttributes) {
    addElementBefore(getSetPayloadSizeVariableElement(splitterAggregatorInfo), splitterElement);
    if (oldAggregatorAttributes.containsKey(OLD_AGGREGATOR_TIMEOUT_ATTRIBUTE)
        && Long.parseLong(oldAggregatorAttributes.get(OLD_AGGREGATOR_TIMEOUT_ATTRIBUTE)) > 0L) {
      addElementBefore(getAggregationCompleteVariableElement(splitterAggregatorInfo, false), splitterElement);
      addVmQueue(splitterAggregatorInfo);
      setAggregatorListenerFlowContent(
                                       addNewFlowAfter(splitterAggregatorInfo.getAggregatorListenerFlowName(),
                                                       getFlow(splitterElement)),
                                       splitterAggregatorInfo);
      if (!oldAggregatorAttributes.containsKey(OLD_AGGREGATOR_FAIL_ON_TIMEOUT_ATTRIBUTE)
          || "true".equals(oldAggregatorAttributes.get(OLD_AGGREGATOR_FAIL_ON_TIMEOUT_ATTRIBUTE))) {
        addElementAfter(getFailOnTimeoutChoiceElement(splitterAggregatorInfo),
                        splitterElement);
      }
      addElementAfter(getVmConsumeElement(splitterAggregatorInfo), splitterElement);
    } else {
      addElementAfter(getSetAggregationPayloadElement(splitterAggregatorInfo), splitterElement);
    }
    addElementBefore(forEachAggregatorElement, splitterElement);
    splitterElement.detach();
  }

  private Element wrapWithForEachAndAggregator(SplitterAggregatorInfo splitterAggregatorInfo,
                                               Map<String, String> oldAggregatorAttributes, List<Element> elements) {
    elements.forEach(Element::detach);
    Element forEachElement = FOR_EACH_TEMPLATE_ELEMENT.clone();
    forEachElement.addContent(elements);
    forEachElement.addContent(getAggregatorElement(splitterAggregatorInfo, oldAggregatorAttributes));
    getForEachCollectionAttribute(splitterAggregatorInfo.getSplitterElement())
        .ifPresent(
                   e -> forEachElement.setAttribute(FOR_EACH_COLLECTION_ATTRIBUTE_KEY, e));
    return forEachElement;
  }

  protected List<Element> collectUntilAggregator(Element splitter) {
    List<Element> splitterAndSiblings = splitter.getParentElement().getChildren();
    List<Element> elementsBetweenSplitterAndAggregator = new LinkedList<>();
    boolean shouldAdd = false;
    for (Element element : splitterAndSiblings) {
      if (element.equals(splitter)) {
        shouldAdd = true;
        continue;
      }
      if (shouldAdd) {
        elementsBetweenSplitterAndAggregator.add(element);
        if (foundMatchingAggregator(element)) {
          break;
        }
      }
    }
    return elementsBetweenSplitterAndAggregator;
  }

  protected Optional<Element> getMatchingAggregatorElement(Element splitter) {
    List<Element> elementsUntilAggregator = collectUntilAggregator(splitter);
    if (!elementsUntilAggregator.isEmpty()) {
      if (foundMatchingAggregator(elementsUntilAggregator.get(elementsUntilAggregator.size() - 1))) {
        return of(elementsUntilAggregator.get(elementsUntilAggregator.size() - 1));
      }
    }
    return empty();
  }

  private boolean foundMatchingAggregator(Element aggregator) {
    return isCustomAggregator(aggregator) || getMatchingAggregatorName().equals(aggregator.getName());
  }

  private boolean isCustomAggregator(Element element) {
    return element != null && "custom-aggregator".equals(element.getName()) && CORE_NAMESPACE.equals(element.getNamespace());
  }

  private void reportOldAggregatorAttributes(List<ReportEntry> reports, Map<String, String> oldAggregatorAttributes,
                                             Element oldAggregator, Element newAggregator) {
    if (oldAggregatorAttributes.containsKey(OLD_AGGREGATOR_PROCESSED_GROUPS_OBJECT_STORE_REF_ATTRIBUTE)) {
      reports.add(new ReportEntry("aggregator.processedGroupsObjectStore", oldAggregator, newAggregator));
    }
    if (oldAggregatorAttributes.containsKey(OLD_AGGREGATOR_EVENT_GROUPS_OBJECT_STORE_REF_ATTRIBUTE)) {
      reports.add(new ReportEntry("aggregator.eventGroupsObjectStore", oldAggregator, newAggregator));
    }
    if (oldAggregatorAttributes.containsKey(OLD_AGGREGATOR_PERSISTENT_STORES_ATTRIBUTE)) {
      reports.add(new ReportEntry("aggregator.persistentStores", oldAggregator, newAggregator));
    }
    if (oldAggregatorAttributes.containsKey(OLD_AGGREGATOR_STORE_PREFIX_ATTRIBUTE)) {
      reports.add(new ReportEntry("aggregator.storePrefix", oldAggregator, newAggregator));
    }

  }

  private void reportCustomAggregator(Element aggregator, MigrationReport report) {
    report.report("aggregator.custom", aggregator, aggregator);
  }

  private void registerNoAggregatorReport(Element splitter, List<ReportEntry> reports) {
    reports.add(new ReportEntry("aggregator.missing", splitter));
  }

  private void registerNeverEnableCorrelationReport(Element splitter, List<ReportEntry> reports) {
    String enableCorrelation = splitter.getAttributeValue("enableCorrelation");
    if ("NEVER".equals(enableCorrelation)) {
      reports.add(new ReportEntry("splitter.neverCorrelationAttribute", splitter));
    }
  }

  //These reports will be written all at once when the document is fully written since the forEach element needs to be created.
  private void writeRegisteredReports(Element forEachElement, List<ReportEntry> reports, MigrationReport migrationReport) {
    reports.forEach(r -> {
      Element reportOn = r.reportOn.orElse(forEachElement);
      Element reportAbout = r.reportAbout;
      String reportKey = r.reportKey;
      migrationReport.report(reportKey, reportAbout, reportOn);
    });
  }

  private Element getAggregatorElement(SplitterAggregatorInfo splitterAggregatorInfo,
                                       Map<String, String> oldAggregatorAttributes) {
    Element aggregationCompleteRoute = new Element("aggregation-complete", AGGREGATORS_NAMESPACE);
    Element newAggregator =
        AGGREGATOR_TEMPLATE
            .clone()
            .setAttribute("name", splitterAggregatorInfo.getAggregatorName())
            .setAttribute("groupSize", "#[vars." + splitterAggregatorInfo.getGroupSizeVariableName() + "]")
            .addContent(aggregationCompleteRoute);
    if (oldAggregatorAttributes.containsKey(OLD_AGGREGATOR_TIMEOUT_ATTRIBUTE)) {
      String timeout = oldAggregatorAttributes.get(OLD_AGGREGATOR_TIMEOUT_ATTRIBUTE);
      newAggregator.setAttribute("timeout", timeout);
      newAggregator.setAttribute("timeoutUnit", "MILLISECONDS");
      aggregationCompleteRoute.getContent().add(0, getAggregationCompleteVariableElement(splitterAggregatorInfo, true));
    } else {
      aggregationCompleteRoute.getContent().add(0, getAggregationVariableElement(splitterAggregatorInfo));
    }
    if (oldAggregatorAttributes.containsKey(OLD_AGGREGATOR_EVENT_GROUPS_OBJECT_STORE_REF_ATTRIBUTE)) {
      newAggregator.setAttribute("objectStore",
                                 oldAggregatorAttributes.get(OLD_AGGREGATOR_EVENT_GROUPS_OBJECT_STORE_REF_ATTRIBUTE));
    }
    return newAggregator;
  }

  private Element setAggregatorListenerFlowContent(Element flow, SplitterAggregatorInfo splitterAggregatorInfo) {
    return flow
        .addContent(
                    AGGREGATOR_LISTENER_TEMPLATE
                        .clone()
                        .setAttribute("aggregatorName", splitterAggregatorInfo.getAggregatorName()))
        .addContent(getVmPublishElement(splitterAggregatorInfo));
  }

  private Element getSetPayloadSizeVariableElement(SplitterAggregatorInfo splitterAggregatorInfo) {
    return SET_VARIABLE_TEMPLATE
        .clone()
        .setAttribute("variableName", splitterAggregatorInfo.getGroupSizeVariableName())
        .setAttribute("value", "#[sizeOf(payload)]");
  }

  private Element getAggregationCompleteVariableElement(SplitterAggregatorInfo splitterAggregatorInfo, boolean value) {
    return SET_VARIABLE_TEMPLATE
        .clone()
        .setAttribute("variableName", splitterAggregatorInfo.getAggregationCompleteVariableName())
        .setAttribute("value", value ? "#[true]" : "#[false]");
  }

  private Element getAggregationVariableElement(SplitterAggregatorInfo splitterAggregatorInfo) {
    return SET_VARIABLE_TEMPLATE
        .clone()
        .setAttribute("variableName", splitterAggregatorInfo.getAggregationVariableName())
        .setAttribute("value", "#[payload]");
  }

  private Element getSetAggregationPayloadElement(SplitterAggregatorInfo splitterAggregatorInfo) {
    return SET_PAYLOAD_TEMPLATE
        .clone()
        .setAttribute("value", "#[vars." + splitterAggregatorInfo.getAggregationVariableName() + "]");
  }

  private Element getVmConsumeElement(SplitterAggregatorInfo splitterAggregatorInfo) {
    return VM_CONSUME_TEMPLATE_ELEMENT
        .clone()
        .setAttribute("config-ref", splitterAggregatorInfo.getVmConfigName())
        .setAttribute("queueName", splitterAggregatorInfo.getVmQueueName());
  }

  private Element getFailOnTimeoutChoiceElement(SplitterAggregatorInfo splitterAggregatorInfo) {
    return CHOICE_TEMPLATE_ELEMENT
        .clone()
        .setContent(WHEN_TEMPLATE_ELEMENT
            .clone()
            .setAttribute("expression", splitterAggregatorInfo.getAggregationCompleteExpression()));
  }

  private Element getVmPublishElement(SplitterAggregatorInfo splitterAggregatorInfo) {
    return VM_PUBLISH_TEMPLATE_ELEMENT
        .clone()
        .setAttribute("config-ref", splitterAggregatorInfo.getVmConfigName())
        .setAttribute("queueName", splitterAggregatorInfo.getVmQueueName());
  }

  private void addVmQueue(SplitterAggregatorInfo splitterAggregatorInfo) {
    Element vmConfig = migrateVmConfig(splitterAggregatorInfo.getSplitterElement(), empty(),
                                       splitterAggregatorInfo.getVmConfigName(), getApplicationModel());
    Element queues = vmConfig.getChild("queues", VM_NAMESPACE);
    queues.addContent(VM_QUEUE_TEMPLATE.clone().setAttribute("queueName", splitterAggregatorInfo.getVmQueueName()));
  }

  private Map<String, String> getOldAggregatorAttributes(Element aggregatorElement) {
    Map<String, String> attributes = new HashMap<>();
    if (aggregatorElement != null) {
      addAttributeToMap(aggregatorElement, OLD_AGGREGATOR_TIMEOUT_ATTRIBUTE, attributes, null);
      addAttributeToMap(aggregatorElement, OLD_AGGREGATOR_FAIL_ON_TIMEOUT_ATTRIBUTE, attributes, "true");
      addAttributeToMap(aggregatorElement, OLD_AGGREGATOR_PROCESSED_GROUPS_OBJECT_STORE_REF_ATTRIBUTE, attributes, null);
      addAttributeToMap(aggregatorElement, OLD_AGGREGATOR_EVENT_GROUPS_OBJECT_STORE_REF_ATTRIBUTE, attributes, null);
      addAttributeToMap(aggregatorElement, OLD_AGGREGATOR_PERSISTENT_STORES_ATTRIBUTE, attributes, null);
      addAttributeToMap(aggregatorElement, OLD_AGGREGATOR_STORE_PREFIX_ATTRIBUTE, attributes, null);
    }
    return attributes;
  }

  private void addAttributeToMap(Element element, String attributeKey, Map<String, String> attributes, String defaultValue) {
    if (defaultValue != null) {
      attributes.put(attributeKey, element.getAttributeValue(attributeKey, defaultValue));
    } else {
      String value = element.getAttributeValue(attributeKey);
      if (value != null) {
        attributes.put(attributeKey, value);
      }
    }
  }

  private static class ReportEntry {

    private Optional<Element> reportOn;
    private Element reportAbout;
    private String reportKey;

    private ReportEntry(String reportKey, Element reportAbout) {
      this.reportKey = reportKey;
      this.reportAbout = reportAbout;
      this.reportOn = empty();
    }

    private ReportEntry(String reportKey, Element reportAbout, Element reportOn) {
      this(reportKey, reportAbout);
      this.reportOn = of(reportOn);
    }
  }

}
