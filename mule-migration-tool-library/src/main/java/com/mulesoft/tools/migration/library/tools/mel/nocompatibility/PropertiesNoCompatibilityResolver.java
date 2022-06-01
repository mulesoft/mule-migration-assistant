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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract class that resolves mel expressions using properties in no compatibility mode.
 *
 * @author Mulesoft Inc.
 * @since 1.4.0
 */
public abstract class PropertiesNoCompatibilityResolver
    implements com.mulesoft.tools.migration.util.CompatibilityResolver<NoCompatibilityResolverResult> {

  private Pattern generalPattern;
  private List<Pattern> singleExpressionPatterns;
  private Pattern patternWithExpression;
  private Pattern patternWithOnlyExpression;

  public PropertiesNoCompatibilityResolver(Pattern generalPattern, List<Pattern> singleExpressionPatterns,
                                           Pattern patternWithExpression, Pattern patternWithOnlyExpression) {
    this.generalPattern = generalPattern;
    this.singleExpressionPatterns = singleExpressionPatterns;
    this.patternWithExpression = patternWithExpression;
    this.patternWithOnlyExpression = patternWithOnlyExpression;
  }

  @Override
  public boolean canResolve(String original) {
    return original != null && generalPattern.matcher(original).find();
  }

  @Override
  public NoCompatibilityResolverResult resolve(String original, Element element, MigrationReport report, ApplicationModel model,
                                               ExpressionMigrator expressionMigrator) {
    String translatedExpression = original;
    boolean success = true;
    if (model.getApplicationGraph() != null) {
      // no compatibility 
      try {
        translatedExpression = translatePropertyReferences(original, element, report, model.getApplicationGraph());
      } catch (MigrationException e) {
        success = false;
      } catch (Exception e) {
        report.report("nocompatibility.unsupportedproperty", element.getParentElement(), element.getParentElement(),
                      element.getName());
        success = false;
      }
    }

    return new NoCompatibilityResolverResult(translatedExpression, success);
  }

  public abstract Map<String, PropertyMigrationContext> getPropertiesContextMap(PropertiesMigrationContext propertiesMigrationContext);

  private String translatePropertyReferences(String expression, Element element,
                                             MigrationReport report, ApplicationGraph applicationGraph)
      throws Exception {
    Element parentElement = element.getParentElement();
    String elementName = element.getName();
    FlowComponent flowComponent = applicationGraph.findFlowComponent(element);
    Matcher matcher = generalPattern.matcher(expression);
    if (flowComponent != null) {
      try {
        if (matcher.find()) {
          return replaceAllOccurencesOfProperty(expression, matcher, flowComponent, report);
        } else {
          matcher = patternWithExpression.matcher(expression);
          if (matcher.find()) {
            report.report("nocompatibility.melexpression", parentElement, parentElement, elementName);
            report.melExpressionFailure(expression);
          }
        }
      } catch (MigrationException e) {
        throw e;
      } catch (Exception e) {
        report.report("nocompatibility.unsupportedproperty", parentElement, parentElement, elementName);
        throw new MigrationException(e.getMessage());
      }
    } else {
      report.report("nocompatibility.unsupportedproperty", parentElement, parentElement, elementName);
      throw new MigrationException("There was an issue trying to resolve expression to no compatibility. Application graph is not correctly populated");
    }

    // nothing to translate
    return expression;
  }

  private String replaceAllOccurencesOfProperty(String content, Matcher outerMatcher, FlowComponent flowComponent,
                                                MigrationReport report)
      throws MigrationException {
    outerMatcher.reset();
    String contentTranslation = content;
    boolean failedCompleteTranslation = false;
    while (outerMatcher.find()) {
      String referenceToProperty = outerMatcher.group();
      Matcher specificPropMatcher = singleExpressionPatterns.get(0).matcher(referenceToProperty);
      int it = 1;
      while (!specificPropMatcher.matches() && it < singleExpressionPatterns.size()) {
        specificPropMatcher = singleExpressionPatterns.get(it).matcher(referenceToProperty);
        it++;
      }

      if (specificPropMatcher.matches()) {
        if (containsExpression(referenceToProperty)) {
          report.report("nocompatibility.unsupportedproperty", flowComponent.getXmlElement().getParentElement(),
                        flowComponent.getXmlElement().getParentElement(), flowComponent.getXmlElement().getName());
          failedCompleteTranslation = true;
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
              report.report("nocompatibility.unsupportedproperty", flowComponent.getXmlElement().getParentElement(),
                            flowComponent.getXmlElement().getParentElement(), flowComponent.getXmlElement().getName());
              failedCompleteTranslation = true;
            }
          }
          contentTranslation = content.replace(specificPropMatcher.group(0), propertyTranslation);
        } catch (Exception e) {
          report.report("nocompatibility.unsupportedproperty", flowComponent.getXmlElement().getParentElement(),
                        flowComponent.getXmlElement().getParentElement(), flowComponent.getXmlElement().getName());
          failedCompleteTranslation = true;
        }
      }

      if (failedCompleteTranslation) {
        throw new MigrationException("Failed to translate all occurencies of properties");
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
