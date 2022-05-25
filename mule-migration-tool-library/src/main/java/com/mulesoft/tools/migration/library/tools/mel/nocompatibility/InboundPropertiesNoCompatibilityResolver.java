/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.tools.mel.nocompatibility;

import com.mulesoft.tools.migration.exception.MigrationException;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.applicationgraph.ApplicationGraph;
import com.mulesoft.tools.migration.project.model.applicationgraph.FlowComponent;
import com.mulesoft.tools.migration.project.model.applicationgraph.PropertyMigrationContext;
import com.mulesoft.tools.migration.project.model.applicationgraph.SourceType;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.CompatibilityResolver;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.Element;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.MIGRATION_NAMESPACE;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Resolver for inbound properties message enrichers
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class InboundPropertiesNoCompatibilityResolver implements CompatibilityResolver<String> {

  private static final Pattern COMPATIBILITY_INBOUND_PATTERN_IN_DW =
      Pattern.compile("(message\\.inboundProperties(?:\\.'?[\\.a-zA-Z0-9]*'?|\\['?.*'+?\\]))");
  private static final Pattern COMPATIBILITY_INBOUND_PATTERN_WITH_BRACKETS =
      Pattern.compile("message\\.inboundProperties\\['(.*?)'\\]");
  private static final Pattern COMPATIBILITY_INBOUND_PATTERN_WITH_DOT =
      Pattern.compile("message\\.inboundProperties\\.'?(.*?)'?");
  private static final Pattern MEL_COMPATIBILITY_INBOUND_PATTERN =
      Pattern.compile("message\\.inboundProperties\\[\'(.*)\'\\]");

  @Override
  public boolean canResolve(String original) {
    return original != null && COMPATIBILITY_INBOUND_PATTERN_IN_DW.matcher(original).find();
  }

  @Override
  public String resolve(String original, Element element, MigrationReport report, ApplicationModel model,
                        ExpressionMigrator expressionMigrator) {
    String translatedExpression = original;
    if (model.getApplicationGraph() != null) {
      // no compatibility 
      try {
        translatedExpression = translateInboundPropertyReferences(original, element, report, model.getApplicationGraph());
        report.melExpressionSuccess(original);
      } catch (Exception e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }

    return translatedExpression;
  }

  private String translateInboundPropertyReferences(String expression, Element element,
                                                    MigrationReport report, ApplicationGraph applicationGraph)
      throws MigrationException {
    Element parentElement = element.getParentElement();
    String elementName = element.getName();
    FlowComponent flowComponent = applicationGraph.findFlowComponent(element);
    Matcher matcher = COMPATIBILITY_INBOUND_PATTERN_IN_DW.matcher(expression);
    if (flowComponent != null) {
      try {
        if (matcher.find()) {
          return replaceAllOccurencesOfProperty(expression, matcher, flowComponent);
        } else {
          matcher = MEL_COMPATIBILITY_INBOUND_PATTERN.matcher(expression);
          if (matcher.find()) {
            report.report("nocompatibility.melexpression", parentElement, parentElement, elementName);
            report.melExpressionFailure(expression);
          }
        }
      } catch (Exception e) {
        report.report("nocompatibility.unsupportedproperty", parentElement, parentElement, elementName);
        report.melExpressionFailure(expression);
        throw e;
      }
    } else {
      report.report("nocompatibility.unsupportedproperty", parentElement, parentElement, elementName);
      report.melExpressionFailure(expression);
      throw new MigrationException("There was an issue trying to resolve expression to no compatibility. Application graph is not correctly populated");
    }


    // nothing to translate
    return expression;
  }

  private String replaceAllOccurencesOfProperty(String content, Matcher outerMatcher, FlowComponent flowComponent)
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
          String propertyTranslation =
              Optional.ofNullable(flowComponent.getPropertiesMigrationContext().getInboundContext().get(propertyToTranslate)).map(
                                                                                                                                  PropertyMigrationContext::getTranslation)
                  .orElse(null);
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
}
