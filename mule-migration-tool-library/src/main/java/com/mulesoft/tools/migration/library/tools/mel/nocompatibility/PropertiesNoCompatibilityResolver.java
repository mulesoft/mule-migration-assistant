/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.tools.mel.nocompatibility;

import com.mulesoft.tools.migration.exception.MigrationException;
import com.mulesoft.tools.migration.library.nocompatibility.PropertyTranslator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.applicationgraph.ApplicationGraph;
import com.mulesoft.tools.migration.project.model.applicationgraph.FlowComponent;
import com.mulesoft.tools.migration.project.model.applicationgraph.PropertiesMigrationContext;
import com.mulesoft.tools.migration.project.model.applicationgraph.PropertyMigrationContext;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.Element;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract class that resolves mel expressions using properties in no compatibility mode
 *
 * @author Mulesoft Inc.
 * @since 1.4.0
 */
public abstract class PropertiesNoCompatibilityResolver
    implements com.mulesoft.tools.migration.util.CompatibilityResolver<String> {

  private Pattern generalPattern;
  private Pattern patternWithBrackets;
  private Pattern patternWithDots;
  private Pattern patternWithExpression;
  private Pattern patternWithOnlyExpression;

  public PropertiesNoCompatibilityResolver(Pattern generalPattern, Pattern patternWithBrackets, Pattern patternWithDots,
                                           Pattern patternWithExpression, Pattern patternWithOnlyExpression) {
    this.generalPattern = generalPattern;
    this.patternWithBrackets = patternWithBrackets;
    this.patternWithDots = patternWithDots;
    this.patternWithExpression = patternWithExpression;
    this.patternWithOnlyExpression = patternWithOnlyExpression;
  }

  @Override
  public boolean canResolve(String original) {
    return original != null && generalPattern.matcher(original).find();
  }

  @Override
  public String resolve(String original, Element element, MigrationReport report, ApplicationModel model,
                        ExpressionMigrator expressionMigrator) {
    String translatedExpression = original;
    if (model.getApplicationGraph() != null) {
      // no compatibility 
      try {
        translatedExpression = translatePropertyReferences(original, element, report, model.getApplicationGraph());
        report.melExpressionSuccess(original);
      } catch (Exception e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }

    return translatedExpression;
  }

  public abstract Map<String, PropertyMigrationContext> getPropertiesContextMap(
                                                                                PropertiesMigrationContext propertiesMigrationContext);

  private String translatePropertyReferences(String expression, Element element,
                                             MigrationReport report, ApplicationGraph applicationGraph)
      throws MigrationException {
    Element parentElement = element.getParentElement();
    String elementName = element.getName();
    FlowComponent flowComponent = applicationGraph.findFlowComponent(element);
    Matcher matcher = generalPattern.matcher(expression);
    if (flowComponent != null) {
      try {
        if (matcher.find()) {
          return replaceAllOccurencesOfProperty(expression, matcher, flowComponent);
        } else {
          matcher = patternWithExpression.matcher(expression);
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
      String referenceToProperty = outerMatcher.group();
      Matcher specificPropMatcher = patternWithBrackets.matcher(referenceToProperty);
      if (!specificPropMatcher.matches()) {
        specificPropMatcher = patternWithDots.matcher(referenceToProperty);
      }

      if (specificPropMatcher.matches()) {
        if (containsExpression(referenceToProperty)) {
          throw new MigrationException("Cannot migrate content, found at least 1 property that can't be translated");
        }

        String propertyToTranslate = specificPropMatcher.group(1);
        try {
          String propertyTranslation =
              Optional.ofNullable(getPropertiesContextMap(flowComponent.getPropertiesMigrationContext()).get(propertyToTranslate))
                  .map(
                       PropertyMigrationContext::getTranslation)
                  .orElse(null);
          if (propertyTranslation == null) {
            propertyTranslation = tryImplicitTranslation(propertyToTranslate, flowComponent);
            if (propertyTranslation == null) {
              throw new MigrationException("Cannot migrate content, found at least 1 property that can't be translated");
            }
          }
          contentTranslation = content.replace(specificPropMatcher.group(0), propertyTranslation);
        } catch (Exception e) {
          throw new MigrationException("Cannot translate content", e);
        }
      }
    }

    return contentTranslation;
  }

  private String tryImplicitTranslation(String propertyToTranslate, FlowComponent component) {
    if (getTranslator() != null) {
      PropertyTranslator translator = getTranslator();
      return translator.translateImplicit(propertyToTranslate, component.getPropertiesMigrationContext().getOriginatingSource());
    }
    return null;
  }

  protected abstract PropertyTranslator getTranslator();

  private boolean containsExpression(String referenceToProperty) {
    return referenceToProperty.matches(patternWithOnlyExpression.pattern());
  }

}
