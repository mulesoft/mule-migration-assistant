package com.mulesoft.tools.migration.library.mule.steps.splitter;

import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.VM_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.migrateVmConfig;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementBefore;
import static java.util.Optional.empty;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;
import org.jdom2.Namespace;

public abstract class AbstractSplitter extends AbstractApplicationModelMigrationStep {

  private static final String AGGREGATORS_NAMESPACE_PREFIX = "aggregators";
  private static final String AGGREGATORS_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/aggregators";
  static final Namespace AGGREGATORS_NAMESPACE = getNamespace(AGGREGATORS_NAMESPACE_PREFIX, AGGREGATORS_NAMESPACE_URI);

  private static final Element SET_VARIABLE_TEMPLATE = new Element("set-variable", CORE_NAMESPACE)
          .setAttribute("value", "#[sizeOf(payload)]");

  private static final Element AGGREGATOR_TEMPLATE = new Element("group-based-aggregator", AGGREGATORS_NAMESPACE);

  private static final Element VM_QUEUE_TEMPLATE = new Element("queue", VM_NAMESPACE);

  private static final Element FOR_EACH_TEMPLATE_ELEMENT = new Element("foreach", CORE_NAMESPACE);

  private static final Element VM_CONSUME_TEMPLATE_ELEMENT = new Element("consume", VM_NAMESPACE);

  protected abstract String getMatchingAggregatorName();

  @Override
  public void execute(Element splitter, MigrationReport report) throws RuntimeException {
    SplitterAggregatorInfo splitterAggregatorInfo = new SplitterAggregatorInfo(splitter, getApplicationModel());
    List<Element> splitterAndSiblings = splitter.getParentElement().getChildren();
    List<Element> elementsBetweenSplitterAndAggregator = new ArrayList<>();
    Element aggregator = null;
    boolean shouldAdd = false;
    handleNeverEnableCorrelation(splitter, report);
    for(Element element : splitterAndSiblings) {
      if(element.equals(splitter)) {
        shouldAdd = true;
        continue;
      }
      if(foundMatchingAggregator(element)) {
        aggregator = element;
        break;
      }
      if(shouldAdd) {
        elementsBetweenSplitterAndAggregator.add(element);
      }
    }
    if(aggregator == null) {
      //Splitter has no aggregator.
    }else if(isCustomAggregator(aggregator)) {
      //Splitter has a custom aggregator
      handleCustomAggregator(splitter, aggregator, report);
    }else {
      //Splitter has a matching aggregator
      addElementBefore(getSetPayloadSizeVariableElement(splitterAggregatorInfo), splitter);
      addElementBefore(wrapWithForEachAndAggregator(splitterAggregatorInfo, elementsBetweenSplitterAndAggregator), splitter);
      addElementAfter(getVmConsumeElement(splitterAggregatorInfo), splitter);
      addVmQueue(splitterAggregatorInfo);
      for(Element element : elementsBetweenSplitterAndAggregator) {
        element.detach();
      }
      aggregator.detach();
      splitter.detach();
    }
  }

  private boolean foundMatchingAggregator(Element element) {
    return isCustomAggregator(element) || getMatchingAggregatorName().equals(element.getName());
  }

  private void handleCustomAggregator(Element splitter,Element aggregator, MigrationReport report) {
    report.report("splitter.aggregator.custom", splitter, aggregator);
  }

  private void handleNeverEnableCorrelation(Element splitter, MigrationReport report) {
    String enableCorrelation = splitter.getAttributeValue("enableCorrelation");
    if("NEVER".equals(enableCorrelation)) {
      report.report("splitter.correlation.never", splitter, splitter);
    }
  }

  private Element wrapWithForEachAndAggregator(SplitterAggregatorInfo splitterAggregatorInfo, List<Element> elements) {
    Element forEachElement = FOR_EACH_TEMPLATE_ELEMENT.clone();
    forEachElement.addContent(elements);
    forEachElement.addContent(getAggregatorElement(splitterAggregatorInfo));
    return forEachElement;
  }

  private boolean isCustomAggregator(Element element) {
    return "custom-aggregator".equals(element.getName());
  }

  private Element getAggregatorElement(SplitterAggregatorInfo splitterAggregatorInfo) {
    return AGGREGATOR_TEMPLATE
            .clone()
            .setAttribute("name", splitterAggregatorInfo.getAggregatorName())
            .setAttribute("groupSize", "#[vars." + splitterAggregatorInfo.getGroupSizeVariableName() + "]")
            .addContent(
                    new Element("aggregation-complete", AGGREGATORS_NAMESPACE)
                            .addContent(new Element("publish", VM_NAMESPACE)
                                                .setAttribute("config-ref",splitterAggregatorInfo.getVmConfigName())
                                                .setAttribute("queueName", splitterAggregatorInfo.getVmQueueName()))
            );
  }

  private Element getSetPayloadSizeVariableElement(SplitterAggregatorInfo splitterAggregatorInfo) {
    return SET_VARIABLE_TEMPLATE
            .clone()
            .setAttribute("variableName", splitterAggregatorInfo.getGroupSizeVariableName());
  }

  private Element getVmConsumeElement(SplitterAggregatorInfo splitterAggregatorInfo) {
    return VM_CONSUME_TEMPLATE_ELEMENT
            .clone()
            .setAttribute("config-ref", splitterAggregatorInfo.getVmConfigName())
            .setAttribute("queueName", splitterAggregatorInfo.getVmQueueName());
  }

  private void addVmQueue(SplitterAggregatorInfo splitterAggregatorInfo) {
    Element vmConfig = migrateVmConfig(splitterAggregatorInfo.getSplitterElement(), empty(), splitterAggregatorInfo.getVmConfigName(), getApplicationModel());
    Element queues = vmConfig.getChild("queues", VM_NAMESPACE);
    queues.addContent(VM_QUEUE_TEMPLATE.clone().setAttribute("queueName", splitterAggregatorInfo.getVmQueueName()));
  }

}
