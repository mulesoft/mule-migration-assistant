package com.mulesoft.tools.migration.library.mule.steps.splitter;

import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.VM_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.VM_SCHEMA_LOCATION;
import static com.mulesoft.tools.migration.project.model.ApplicationModel.addNameSpace;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementBefore;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;
import org.jdom2.Namespace;

public class AbstractSplitter extends AbstractApplicationModelMigrationStep {

  private static final String XPATH_SELECTOR = "//*[local-name()='collection-splitter']";

  private static final String AGGREGATORS_NAMESPACE_PREFIX = "aggregators";
  private static final String AGGREGATORS_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/aggregators";
  static final Namespace AGGREGATORS_NAMESPACE = getNamespace(AGGREGATORS_NAMESPACE_PREFIX, AGGREGATORS_NAMESPACE_URI);

  private static final Element AGGREGATOR_TEMPLATE_ELEMENT =
          new Element("group-based-aggregator", AGGREGATORS_NAMESPACE)
                  .setAttribute("name", "someName")
                  .setAttribute("groupId", "ID")
                  .setAttribute("groupSize","size")
                  .setAttribute("evictionTime", "0")
                  .addContent(
                          new Element("aggregation-complete", AGGREGATORS_NAMESPACE)
                                  .addContent(new Element("publish", VM_NAMESPACE)
                                                      .setAttribute("config-ref","config")
                                                      .setAttribute("queueName", "someQueue"))
                  );

  private static final Element FOR_EACH_TEMPLATE_ELEMENT = new Element("foreach", CORE_NAMESPACE);

  private static final Element VM_CONSUME_TEMPLATE_ELEMENT =
          new Element("consume", VM_NAMESPACE)
                  .setAttribute("config-ref", "vm")
                  .setAttribute("queueName", "queue");

  private VmInformation vmInformation;

  public AbstractSplitter(VmInformation vmInformation) {
    this.setAppliedTo(XPATH_SELECTOR);
    this.vmInformation = vmInformation;
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    vmInformation.addQueue(object.toString());
    List<Element> objectAndSiblings = object.getParentElement().getChildren();
    List<Element> elementsBetweenSplitterAndAggregator = new ArrayList<>();
    boolean shouldAdd = false;
    for(Element element : objectAndSiblings) {
      if(element.equals(object)) {
        shouldAdd = true;
        continue;
      }
      if(element.getName().equals("collection-aggregator")) {
        element.detach();
        break;
      }
      if(shouldAdd) {
        element.detach();
        elementsBetweenSplitterAndAggregator.add(element);
      }
    }
    addElementBefore(insertIntoForEachAndAggregator(elementsBetweenSplitterAndAggregator), object);
    addElementAfter(VM_CONSUME_TEMPLATE_ELEMENT.clone(), object);
    object.detach();
  }

  private Element insertIntoForEachAndAggregator(List<Element> elements) {
    Element forEachElement = FOR_EACH_TEMPLATE_ELEMENT.clone();
    forEachElement.addContent(elements);
    forEachElement.addContent(AGGREGATOR_TEMPLATE_ELEMENT.clone());
    return forEachElement;
  }

}
