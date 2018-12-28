package com.mulesoft.tools.migration.library.mule.steps.splitter;

import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.VM_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.migrateVmConfig;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementBefore;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getFlow;
import static java.nio.file.Paths.get;
import static java.util.Optional.empty;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Parent;

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

  protected abstract String getSplitterName();

  @Override
  public void execute(Element splitter, MigrationReport report) throws RuntimeException {
    List<Element> objectAndSiblings = splitter.getParentElement().getChildren();
    List<Element> elementsBetweenSplitterAndAggregator = new ArrayList<>();
    boolean shouldAdd = false;
    for(Element element : objectAndSiblings) {
      if(element.equals(splitter)) {
        shouldAdd = true;
        continue;
      }
      if(getMatchingAggregatorName().equals(element.getName())) {
        element.detach();
        break;
      }
      if(shouldAdd) {
        element.detach();
        elementsBetweenSplitterAndAggregator.add(element);
      }
    }
    addElementBefore(getSetPayloadSizeVariableElement(splitter), splitter);
    addElementBefore(wrapWithForEachAndAggregator(splitter, elementsBetweenSplitterAndAggregator), splitter);
    addElementAfter(VM_CONSUME_TEMPLATE_ELEMENT.clone(), splitter);
    splitter.detach();
  }

  private Element wrapWithForEachAndAggregator(Element splitter, List<Element> elements) {
    Element forEachElement = FOR_EACH_TEMPLATE_ELEMENT.clone();
    forEachElement.addContent(elements);
    forEachElement.addContent(getAggregatorElement(splitter));
    return forEachElement;
  }

  private boolean isCustomAggregator(String elementName) {
    return "custom-aggregator".equals(elementName);
  }

  private Element getAggregatorElement(Element splitter) {
    return AGGREGATOR_TEMPLATE
            .clone()
            .setAttribute("name", getAggregatorName(splitter))
            .addContent(
                    new Element("aggregation-complete", AGGREGATORS_NAMESPACE)
                            .addContent(new Element("publish", VM_NAMESPACE)
                                                .setAttribute("config-ref",getVmConfigName(splitter))
                                                .setAttribute("queueName", getVMQueueName(splitter)))
            );
  }

  private Element getSetPayloadSizeVariableElement(Element splitter) {
    return SET_VARIABLE_TEMPLATE
            .clone()
            .setAttribute("variableName", getGrupSizeVariableName(splitter));
  }

  private Element getVmConsumeElement(Element splitter) {
    return VM_CONSUME_TEMPLATE_ELEMENT
            .clone()
            .setAttribute("config-ref", getVmConfigName(splitter))
            .setAttribute("queueName", getVMQueueName(splitter));
  }

  private String getGrupSizeVariableName(Element splitter) {
    return this.getSplitterUniqueId(splitter) + "group-size";
  }

  private String getAggregatorName(Element splitter) {
    return this.getSplitterUniqueId(splitter) + "" + "-aggregator";
  }

  private String getVMQueueName(Element splitter) {
    return this.getSplitterUniqueId(splitter) + "" + "-vm-queue";
  }

  private String getVmConfigName(Element splitter) {
    return splitter.getDocument().getBaseURI();
  }

  private String getDocumentRelativePath(Element splitter) {
    return get(getApplicationModel().getProjectBasePath().toUri().toString()).relativize(get(splitter.getDocument().getBaseURI())).toString();
  }

  private String getSplitterUniqueId(Element splitter) {
    int id = 31;
    String documentId = getDocumentRelativePath(splitter);
    Element flow = getFlow(splitter);
    String flowName = flow.getName();
    Element parent = splitter.getParentElement();
    id = 31 * id + documentId.hashCode();
    id = 31 * id + flowName.hashCode();
    while(parent != flow) {
      id = 31 * id + Objects.hashCode(parent.getChildren());
      parent = parent.getParentElement();
    }
    return getSplitterName() + id;
  }

  private void addVmQueue(Element splitter) {
    Element vmConfig = migrateVmConfig(splitter, empty(), getVmConfigName(splitter), getApplicationModel());
    Element queues = vmConfig.getChild("queues");
    queues.addContent(VM_QUEUE_TEMPLATE.clone().setAttribute("name", getVMQueueName(splitter)));
  }

}
