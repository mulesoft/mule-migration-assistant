package com.mulesoft.tools.migration.library.mule.steps.nocompatibility;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mulesoft.tools.migration.library.applicationflow.Flow;
import com.mulesoft.tools.migration.library.applicationflow.MessageProcessor;
import com.mulesoft.tools.migration.library.applicationflow.MessageSource;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathFactory;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.*;

public class CreateApplicationGraphStep implements MigrationStep<ApplicationModel>, ExpressionMigratorAware {

  public static final String FLOW_XPATH = getAllElementsFromNamespaceXpathSelector(CORE_NS_URI, ImmutableList.of("flow", "sub-flow"), true, false);
  public static final String MESSAGE_SOURCE_FILTER_EXPRESSION =
      getChildElementsWithAttribute("", "isMessageSource", "\"true\"", true);
  private static final String FLOW_REF_EXPRESSION = getAllElementsFromNamespaceXpathSelector(CORE_NS_URI, ImmutableList.of("flow-ref"), false, true);
  private static final Pattern COMPATIBILITY_INBOUND_PATTERN_IN_DW =
      Pattern.compile("#\\[.*(vars.compatibility_inboundProperties(?:.*|\\[.*\\])).*\\]");
  private static final Pattern COMPATIBILITY_INBOUND_PATTERN =
      Pattern.compile("(?:vars.compatibility_inboundProperties.(.*)|vars.compatibility_inboundProperties.\\[(.*)\\])");
  private static final Pattern MEL_COMPATIBILITY_INBOUND_PATTERN =
      Pattern.compile("message.inboundProperties.\\[\'(.*)\'\\]");
   private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Step to create a graph of the application to understand the flow of properties";
  }

  @Override
  public void execute(ApplicationModel applicationModel, MigrationReport report) throws RuntimeException {
    List<Document> applicationDocuments = Lists.newArrayList(applicationModel.getApplicationDocuments().values());
    List<Flow> applicationFlows = applicationDocuments.stream()
        .map(this::getFlows)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    Graph<Flow, DefaultEdge> applicationGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
    applicationFlows.forEach(flow -> applicationGraph.addVertex(flow));

    // build ApplicationGraph
    for (Flow flow : applicationFlows) {

      // get explicit connections
      List<Flow> connectedFlows = getFlowRefElements(flow, applicationGraph, report);
      connectedFlows.forEach(connectedFlow -> {
        if (!applicationGraph.containsVertex(connectedFlow)) {
          applicationGraph.addVertex(connectedFlow);
        }

        applicationGraph.addEdge(flow, connectedFlow);
      });

      // TODO: add implicit connections (i.e: operations)
    }

    // resolve inbound references
    applicationFlows.forEach(flow -> {
      List<MessageProcessor> messageProcessors = getMessageProcessors(flow);
      messageProcessors.forEach(messageProcessor -> {
        List<MessageSource> sources = findMessageSources(flow, applicationGraph);
        Element element = messageProcessor.getXmlElement();
        updatePropertyReferencesInAttributes(element.getAttributes(), sources.stream()
            .map(MessageSource::getType).collect(Collectors.toList()));

        // TODO: add for children

      });
      
      removeCompatibilityElements(flow);
    });
  }

  private void removeCompatibilityElements(Flow flow) {
    flow.getXmlElement().removeChildren("attributes-to-inbound-properties", COMPATIBILITY_NAMESPACE);
  }

  private List<MessageSource> findMessageSources(Flow flow, Graph<Flow, DefaultEdge> applicationGraph) {
    List<MessageSource> sources = Lists.newArrayList();
    Queue<Flow> incomingFlows = new LinkedList<>();
    incomingFlows.offer(flow);
    Flow currentFlow = flow;
    while (!incomingFlows.isEmpty()) {
      currentFlow = incomingFlows.poll();
      if (currentFlow.hasSource()) {
        sources.add(currentFlow.getMessageSource());
      } else {
        applicationGraph.incomingEdgesOf(currentFlow).stream()
            .map(e -> applicationGraph.getEdgeSource(e))
            .forEach(incomingFlows::offer);
      }
    }

    return sources;
  }

  private void updatePropertyReferencesInAttributes(List<Attribute> attributes, List<String> originatingSources) {
    attributes.forEach(attribute -> attribute.setValue(translatePropertyReferences(attribute.getValue(), originatingSources)));
  }

  private String translatePropertyReferences(String content, List<String> originatingSources) {
    Matcher matcher = COMPATIBILITY_INBOUND_PATTERN_IN_DW.matcher(content);
    if (matcher.matches()) {
      for (int i = 1; i <= matcher.groupCount(); i++) {
        String referenceToInbound = matcher.group(i);
        Matcher specificVarMatcher = COMPATIBILITY_INBOUND_PATTERN.matcher(referenceToInbound);
        if (specificVarMatcher.matches()) {
          String propertyToTranslate = specificVarMatcher.group(1);
          String propertyTranslation = InboundToAttributesTranslator.translate(originatingSources, propertyToTranslate);
          if (propertyTranslation != null) {
            return content.replaceAll(specificVarMatcher.group(0), propertyTranslation);
          } else {
            // TODO: this is a user defined property somewhere upstream, we need to see how to handle
          }
        }
      }
    } else {
      matcher = MEL_COMPATIBILITY_INBOUND_PATTERN.matcher(content);
      if (matcher.matches()) {
        String propertyToTranslate = matcher.group(1);
        String propertyTranslation = InboundToAttributesTranslator.translate(originatingSources, propertyToTranslate);
        // TODO: case using mel, we need to use the translation and transform to DW 
        return content.replaceAll(matcher.group(0), propertyTranslation);
      }
    }

    // nothing to translate
    return content;
  }

  private List<Flow> getFlowRefElements(Flow flow, Graph<Flow, DefaultEdge> applicationGrap, MigrationReport report) {
    List<Element> flowRefsAsXml = getChildrenMatchingExpression(flow.getXmlElement(), FLOW_REF_EXPRESSION);
    List<Flow> flowRefs = Lists.newArrayList();
    flowRefsAsXml.forEach(flowRef -> {
      if (!expressionMigrator.isWrapped(flowRef.getAttribute("name").getValue())) {
        flowRefs.add(
                     applicationGrap.vertexSet().stream().filter(v -> v.getName().equals(flowRef.getAttribute("name").getValue()))
                         .findFirst()
                         .orElseThrow(() -> new RuntimeException("Cannot find referenced flow in current application")));
      } else {
        // TODO: report as error because of dynamic flow ref
        report.report("nocompatibility.dynamicflowref", flowRef, flowRef);
      }
    });

    return flowRefs;
  }

  private List<Flow> getFlows(Document document) {
    List<Element> flowsAsXml = getChildrenMatchingExpression(document.getRootElement(), FLOW_XPATH);

    return flowsAsXml.stream()
        .map(this::convertToFlow)
        .collect(Collectors.toList());
  }

  private Flow convertToFlow(Element flowAsXml) {
    return new Flow(flowAsXml, getMessageSource(flowAsXml));
  }

  private MessageSource getMessageSource(Element flowAsXml) {
    List<Element> messageSource = getChildrenMatchingExpression(flowAsXml, MESSAGE_SOURCE_FILTER_EXPRESSION);
    if (messageSource.isEmpty()) {
      return null;
    }
    
    Element messageSourceXml = Iterables.getOnlyElement(messageSource);
    return new MessageSource(messageSourceXml);
  }

  private List<MessageProcessor> getMessageProcessors(Flow parentFlow) {
    Element flowAsXmL = parentFlow.getXmlElement();
    return flowAsXmL.getContent().stream()
        .filter(Element.class::isInstance)
        .filter(e -> !e.equals(Optional.ofNullable(parentFlow.getMessageSource()).map(MessageSource::getXmlElement).orElse(null)))
        .map(Element.class::cast)
        .map(xmlElement -> new MessageProcessor(xmlElement, parentFlow))
        .collect(Collectors.toList());
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
