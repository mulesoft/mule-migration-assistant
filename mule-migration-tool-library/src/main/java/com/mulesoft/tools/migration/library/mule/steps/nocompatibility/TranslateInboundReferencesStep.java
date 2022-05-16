/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.nocompatibility;

import com.mulesoft.tools.migration.exception.MigrationException;
import com.mulesoft.tools.migration.project.model.applicationgraph.*;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.CDATA;
import org.jdom2.Content;
import org.jdom2.Element;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NAMESPACE;

/**
 * Step to translate inbound property references
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public class TranslateInboundReferencesStep extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  private static final Pattern COMPATIBILITY_INBOUND_PATTERN_IN_DW =
      Pattern.compile("(vars\\.compatibility_inboundProperties(?:\\.'?[\\.a-zA-Z0-9]*'?|\\['?.*'+?\\]))");
  private static final Pattern COMPATIBILITY_INBOUND_PATTERN_WITH_BRACKETS =
      Pattern.compile("vars\\.compatibility_inboundProperties\\['(.*?)'\\]");
  private static final Pattern COMPATIBILITY_INBOUND_PATTERN_WITH_DOT =
      Pattern.compile("vars\\.compatibility_inboundProperties\\.'?(.*?)'?");
  private static final Pattern MEL_COMPATIBILITY_INBOUND_PATTERN =
      Pattern.compile("message\\.inboundProperties\\[\'(.*)\'\\]");

  private ExpressionMigrator expressionMigrator;
  private InboundToAttributesTranslator translator;

  public TranslateInboundReferencesStep() {
    this.translator = new InboundToAttributesTranslator();
    this.setAppliedTo("*");
  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return this.expressionMigrator;
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public void execute(Element unused, MigrationReport report) throws RuntimeException {
    // only works if "no compatibility mode" is on, which means the application graph exists
    if (getApplicationModel().getApplicationGraph() != null) {
      ApplicationGraph applicationGraph = getApplicationModel().getApplicationGraph();
      // resolve inbound references per flow
      // TODO: this needs to be moved into actual steps
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

      applicationGraph.getAllStartingFlowComponents().stream()
          .map(startingPoint -> applicationGraph.getAllFlowComponentsOfTypeAlongPath(startingPoint, MessageProcessor.class,
                                                                                     COMPATIBILITY_NAMESPACE.getPrefix() + '_'
                                                                                         + "attributes-to-inbound-properties"))
          .flatMap(Collection::stream)
          .forEach(mp -> mp.getXmlElement().detach());
    }
  }

  private Optional<CDATA> getCDATAContent(List<Content> content) {
    return content.stream().filter(CDATA.class::isInstance).map(CDATA.class::cast).findFirst();
  }

  private void updatePropertyReferencesInAttributes(Element parentElement, SourceType originatingSourceType,
                                                    MigrationReport report) {
    parentElement.getAttributes()
        .forEach(attribute -> attribute
            .setValue(translatePropertyReferences(attribute.getParent(), attribute.getName(), attribute.getValue(),
                                                  originatingSourceType, report)));
  }

  private void updatePropertyReferencesInCDATAContent(Element parentElement, CDATA cdata, SourceType originatingSourceType,
                                                      MigrationReport report) {
    cdata.setText(translatePropertyReferences(parentElement, "CDATA", cdata.getText(), originatingSourceType, report));
  }

  private String translatePropertyReferences(Element parentElement, String elementName, String content,
                                             SourceType originatingSourceType,
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

  private String replaceAllOccurencesOfProperty(String content, Matcher outerMatcher, SourceType originatingSourceType)
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

  @Override
  public boolean shouldReportMetrics() {
    return false;
  }
}
