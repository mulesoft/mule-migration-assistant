package com.mulesoft.tools.migration.library.mule.steps.splitter;

import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.VM_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.migrateVmConfig;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementBefore;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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
    List<ReportEntry> reports = new LinkedList<>();
    Element forEachElement = null;

    //Check if enableCorrelation=NEVER and report it
    reportNeverEnableCorrelation(splitter, reports);

    List<Element> elementsBetweenSplitterAndAggregator = collectUntilAggregator(splitter);
    Element aggregatorElement = foundMatchingAggregator(elementsBetweenSplitterAndAggregator.get(elementsBetweenSplitterAndAggregator.size() - 1)) ?
                                elementsBetweenSplitterAndAggregator.remove(elementsBetweenSplitterAndAggregator.size() - 1) :
                                null;

    if(!isCustomAggregator(aggregatorElement)) {
      if(aggregatorElement == null) {
        //There is no related aggregator
        reportNoAggregator(splitter, reports);
      }else {
        aggregatorElement.detach();
      }
      //Splitter has a matching aggregator
      forEachElement = wrapWithForEachAndAggregator(splitterAggregatorInfo, elementsBetweenSplitterAndAggregator);
      replaceInDocument(splitter, forEachElement, splitterAggregatorInfo);
    }else {
      //Splitter has a custom aggregator
      reportCustomAggregator(aggregatorElement, report);
    }
    //Write collected reports
    writeReports(forEachElement, reports, report);
  }

  private void replaceInDocument(Element splitterElement, Element forEachAggregatorElement, SplitterAggregatorInfo splitterAggregatorInfo) {
    addElementBefore(getSetPayloadSizeVariableElement(splitterAggregatorInfo), splitterElement);
    addElementBefore(forEachAggregatorElement, splitterElement);
    addElementAfter(getVmConsumeElement(splitterAggregatorInfo), splitterElement);
    addVmQueue(splitterAggregatorInfo);
    splitterElement.detach();
  }

  private Element wrapWithForEachAndAggregator(SplitterAggregatorInfo splitterAggregatorInfo, List<Element> elements) {
    elements.forEach(Element::detach);
    Element forEachElement = FOR_EACH_TEMPLATE_ELEMENT.clone();
    forEachElement.addContent(elements);
    forEachElement.addContent(getAggregatorElement(splitterAggregatorInfo));
    return forEachElement;
  }

  private List<Element> collectUntilAggregator(Element splitter) {
    List<Element> splitterAndSiblings = splitter.getParentElement().getChildren();
    List<Element> elementsBetweenSplitterAndAggregator = new LinkedList<>();
    boolean shouldAdd = false;
    for(Element element : splitterAndSiblings) {
      if(element.equals(splitter)) {
        shouldAdd = true;
        continue;
      }
      if(shouldAdd) {
        elementsBetweenSplitterAndAggregator.add(element);
        if(foundMatchingAggregator(element)) {
          break;
        }
      }
    }
    return elementsBetweenSplitterAndAggregator;
  }

  private boolean foundMatchingAggregator(Element element) {
    return isCustomAggregator(element) || getMatchingAggregatorName().equals(element.getName());
  }

  private boolean isCustomAggregator(Element element) {
    return element != null && "custom-aggregator".equals(element.getName());
  }

  private void reportCustomAggregator(Element aggregator, MigrationReport report) {
    report.report("splitter.aggregator.custom", aggregator, aggregator);
  }

  private void reportNoAggregator(Element splitter, List<ReportEntry> reports) {
    reports.add(new ReportEntry("splitter.aggregator.missing", splitter));
  }

  private void reportNeverEnableCorrelation(Element splitter, List<ReportEntry> reports) {
    String enableCorrelation = splitter.getAttributeValue("enableCorrelation");
    if ("NEVER".equals(enableCorrelation)) {
      reports.add(new ReportEntry("splitter.correlation.never", splitter));
    }
  }

  //These reports will be written all at once when the document is fully written since the forEach element needs to be created.
  private void writeReports(Element forEachElement, List<ReportEntry> reports, MigrationReport migrationReport) {
    reports.forEach(r -> {
      Element reportOn = r.reportOn.orElse(forEachElement);
      Element reportAbout = r.reportAbout;
      String reportKey = r.reportKey;
      migrationReport.report(reportKey, reportAbout, reportOn);
    });
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
