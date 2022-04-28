package com.mulesoft.tools.migration.library.mule.steps.nocompatibility;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mulesoft.tools.migration.exception.MigrationException;
import com.mulesoft.tools.migration.library.applicationflow.*;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.*;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.*;

public class CreateApplicationGraphStep implements MigrationStep<ApplicationModel>, ExpressionMigratorAware {

  public static final String FLOW_XPATH =
      getAllElementsFromNamespaceXpathSelector(CORE_NS_URI, ImmutableList.of("flow", "sub-flow"), true, false);
  public static final String MESSAGE_SOURCE_FILTER_EXPRESSION =
      getChildElementsWithAttribute("", "isMessageSource", "\"true\"", true);
  private static final String FLOW_REF_EXPRESSION =
      getAllElementsFromNamespaceXpathSelector(CORE_NS_URI, ImmutableList.of("flow-ref"), false, true);
  private static final Pattern COMPATIBILITY_INBOUND_PATTERN_IN_DW =
      Pattern.compile("(vars\\.compatibility_inboundProperties(?:\\.'?[\\.a-zA-Z0-9]*'?|\\['?.*'+?\\]))");
  private static final Pattern COMPATIBILITY_INBOUND_PATTERN_WITH_BRACKETS =
      Pattern.compile("vars\\.compatibility_inboundProperties\\['(.*?)'\\]");
  private static final Pattern COMPATIBILITY_INBOUND_PATTERN_WITH_DOT =
      Pattern.compile("vars\\.compatibility_inboundProperties\\.'?(.*?)'?");
  private static final Pattern MEL_COMPATIBILITY_INBOUND_PATTERN =
      Pattern.compile("message\\.inboundProperties\\[\'(.*)\'\\]");
  private static final List<String> SUPPORTED_OPERATIONS = Lists.newArrayList("http:request");
  private ExpressionMigrator expressionMigrator;
  private InboundToAttributesTranslator translator;

  @Override
  public String getDescription() {
    return "Step to create a graph of the application to understand the flow of properties";
  }

  public CreateApplicationGraphStep() {
    this.translator = new InboundToAttributesTranslator();
  }

  @Override
  public void execute(ApplicationModel applicationModel, MigrationReport report) throws RuntimeException {
    List<Document> applicationDocuments = Lists.newArrayList(applicationModel.getApplicationDocuments().values());
    List<Flow> applicationFlows = applicationDocuments.stream()
        .map(this::getFlows)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    ApplicationGraph applicationGraph = new ApplicationGraph();

    applicationFlows.forEach(flow -> {
      List<FlowComponent> flowComponents = getFlowComponents(flow, applicationFlows, report);
      applicationGraph.addConnectedFlowComponents(flowComponents);
    });

    // build ApplicationGraph based on flow components and flowRefs. 
    // This graph can have non connected components, if we have multiple flows with sources
    // get explicit connections (flow-refs)
    Map<FlowRef, FlowComponent> connectedFlows = getFlowRefMap(applicationGraph);
    connectedFlows.entrySet().forEach(connectedFlow -> {
      applicationGraph.addEdge(connectedFlow.getKey(), connectedFlow.getValue());
    });

    // TODO: add implicit connections (i.e: operations)
    // Is this actually needed? If we have an operation that calls a flow inside the same app, then handling
    // any property references should be the same as migrating that flow individually

    // resolve inbound references per flow
    applicationGraph.getAllFlowComponents().forEach(flowComponent -> {
      //TODO: change to only find the source when we have to do a property translation in a flow component
      Optional<PropertiesSource> propertiesEmitter = applicationGraph.findClosestPropertiesSource(flowComponent);
      if (propertiesEmitter.isPresent()) {
        Queue<Element> elementsToUpdate = new LinkedList<>();
        elementsToUpdate.offer(flowComponent.getXmlElement());

        while (!elementsToUpdate.isEmpty()) {
          Element element = elementsToUpdate.poll();
          // update references in attributes
          updatePropertyReferencesInAttributes(element, propertiesEmitter.get().getType(), report);
          Optional<CDATA> cdataContent = getCDATAContent(element.getContent());
          if (element.getChildren().isEmpty() && cdataContent.isPresent()) {
            updatePropertyReferencesInCDATAContent(element, cdataContent.get(), propertiesEmitter.get().getType(), report);
          } else {
            element.getChildren().forEach(e -> elementsToUpdate.offer(e));
          }
        }
      } else {
        // TODO: unresolvable property
      }
    });

    applicationFlows.forEach(flow -> removeCompatibilityElements(flow));
  }

  private List<FlowComponent> getFlowComponents(Flow flow, List<Flow> applicationFlows, MigrationReport report) {
    Element flowAsXmL = flow.getXmlElement();
    List<FlowComponent> flowComponents = Lists.newArrayList();
    MessageSource messageSource = getMessageSource(flow);
    if (messageSource != null) {
      flowComponents.add(messageSource);
    }

    List<MessageProcessor> processors = flowAsXmL.getContent().stream()
        .filter(Element.class::isInstance)
        .map(Element.class::cast)
        .filter(e -> messageSource == null || e != messageSource.getXmlElement())
        .map(xmlElement -> convertToComponent(xmlElement, flow, applicationFlows, report))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    flowComponents.addAll(processors);
    return flowComponents;
  }

  private MessageProcessor convertToComponent(Element xmlElement, Flow parentFlow,
                                              List<Flow> applicationFlows, MigrationReport report) {
    if (xmlElement.getName().equals("flow-ref")) {
      if (!expressionMigrator.isWrapped(xmlElement.getAttribute("name").getValue())) {
        String destinationFlowName = xmlElement.getAttribute("name").getValue();
        Optional<Flow> destinationFlow = applicationFlows.stream()
            .filter(flow -> flow.getName().equals(destinationFlowName))
            .findFirst();

        if (destinationFlow.isPresent()) {
          return new FlowRef(xmlElement, parentFlow, destinationFlow.get());
        } else {
          report.report("nocompatibility.missingflow", xmlElement, xmlElement);
        }
      } else {
        report.report("nocompatibility.dynamicflowref", xmlElement, xmlElement);
      }
    } else if (isOperation(xmlElement)) {
      return new MessageOperation(xmlElement, parentFlow);
    } else {
      return new MessageProcessor(xmlElement, parentFlow);
    }

    return null;
  }

  private boolean isOperation(Element xmlElement) {
    return SUPPORTED_OPERATIONS.contains(String.format("%s:%s", xmlElement.getNamespacePrefix(), xmlElement.getName()));
  }

  private Optional<CDATA> getCDATAContent(List<Content> content) {
    return content.stream().filter(CDATA.class::isInstance).map(CDATA.class::cast).findFirst();
  }

  private void removeCompatibilityElements(Flow flow) {
    flow.getXmlElement().removeChildren("attributes-to-inbound-properties", COMPATIBILITY_NAMESPACE);
  }

  private void updatePropertyReferencesInAttributes(Element parentElement,
                                                    InboundToAttributesTranslator.SourceType originatingSourceType,
                                                    MigrationReport report) {
    parentElement.getAttributes()
        .forEach(attribute -> attribute
            .setValue(translatePropertyReferences(attribute.getParent(), attribute.getName(), attribute.getValue(),
                                                  originatingSourceType, report)));
  }

  private void updatePropertyReferencesInCDATAContent(Element parentElement, CDATA cdata,
                                                      InboundToAttributesTranslator.SourceType originatingSourceType,
                                                      MigrationReport report) {
    cdata.setText(translatePropertyReferences(parentElement, "CDATA", cdata.getText(), originatingSourceType, report));
  }

  private String translatePropertyReferences(Element parentElement, String elementName, String content,
                                             InboundToAttributesTranslator.SourceType originatingSourceType,
                                             MigrationReport report) {
    Matcher matcher = COMPATIBILITY_INBOUND_PATTERN_IN_DW.matcher(content);
    try {
      if (matcher.find()) {
        return replaceAllOccurencesOfProperty(content, matcher, originatingSourceType);
      } else {
        matcher = MEL_COMPATIBILITY_INBOUND_PATTERN.matcher(content);
        if (matcher.find()) {
          report.report("nocompatibility.melexpression", parentElement, parentElement, elementName);
        }
      }
    } catch (Exception e) {
      report.report("nocompatibility.unsupportedproperty", parentElement, parentElement, elementName);
    }

    // nothing to translate
    return content;
  }

  private String replaceAllOccurencesOfProperty(String content, Matcher outerMatcher,
                                                InboundToAttributesTranslator.SourceType originatingSourceType)
      throws MigrationException {
    outerMatcher.reset();
    String contentTranslation = content;
    while (outerMatcher.find()) {
      String referenceToInbound = outerMatcher.group();
      Matcher specificVarMatcher = COMPATIBILITY_INBOUND_PATTERN_WITH_BRACKETS.matcher(referenceToInbound);
      if (specificVarMatcher.matches()) {
        if (containsExpression(referenceToInbound)) {
          throw new MigrationException("Cannot migrate content, found at least 1 property that can't be translated");
        }
      } else {
        specificVarMatcher = COMPATIBILITY_INBOUND_PATTERN_WITH_DOT.matcher(referenceToInbound);
      }

      if (specificVarMatcher.matches()) {
        String propertyToTranslate = specificVarMatcher.group(1);
        try {
          String propertyTranslation = translator.translate(originatingSourceType, propertyToTranslate);
          if (propertyTranslation != null) {
            contentTranslation = content.replace(specificVarMatcher.group(0), propertyTranslation);
          } else {
            throw new MigrationException("Cannot migrate content, found at least 1 property that can't be translated");
          }
        } catch (Exception e) {
          throw new MigrationException("Cannot translate content", e);
        }
      }
    }

    return contentTranslation;
  }

  private boolean containsExpression(String referenceToInbound) {
    return referenceToInbound.matches("vars\\.compatibility_inboundProperties\\[[^'].*\\]");
  }

  private Map<FlowRef, FlowComponent> getFlowRefMap(ApplicationGraph applicationGraph) {
    Map<FlowRef, FlowComponent> connectedFlows = Maps.newHashMap();
    applicationGraph.getAllFlowComponents(FlowRef.class).forEach(flowRef -> {
      FlowComponent destinationFlowComponent = applicationGraph.getStartingFlowComponent(flowRef.getDestinationFlow());
      connectedFlows.put(flowRef, destinationFlowComponent);
    });

    return connectedFlows;
  }

  private List<Flow> getFlows(Document document) {
    List<Element> flowsAsXml = getChildrenMatchingExpression(document.getRootElement(), FLOW_XPATH);

    return flowsAsXml.stream()
        .map(this::convertToFlow)
        .collect(Collectors.toList());
  }

  private Flow convertToFlow(Element flowAsXml) {
    return new Flow(flowAsXml);
  }

  private MessageSource getMessageSource(Flow flow) {
    List<Element> messageSource = getChildrenMatchingExpression(flow.getXmlElement(), MESSAGE_SOURCE_FILTER_EXPRESSION);
    if (messageSource.isEmpty()) {
      return null;
    }

    Element messageSourceXml = Iterables.getOnlyElement(messageSource);
    return new MessageSource(messageSourceXml, flow);
  }

  private List<Element> getChildrenMatchingExpression(Element elementToEvaluate, String expression) {
    return XPathFactory.instance().compile(expression, Filters.element()).evaluate(elementToEvaluate);
  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }
}
