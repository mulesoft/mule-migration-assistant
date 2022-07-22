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
import com.mulesoft.tools.migration.project.model.applicationgraph.PropertiesMigrationContext;
import com.mulesoft.tools.migration.project.model.applicationgraph.PropertyTranslator;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.Element;

/**
 * Abstract class that resolves mel expressions using properties in no compatibility mode.
 *
 * @author Mulesoft Inc.
 * @since 1.4.0
 */
public abstract class PropertiesNoCompatibilityResolver
    implements com.mulesoft.tools.migration.util.CompatibilityResolver<NoCompatibilityResolverResult> {

  private Pattern mapPattern;
  private Pattern generalPattern;
  private List<Pattern> singleExpressionPatterns;
  private Pattern patternWithExpression;
  private Pattern patternWithOnlyExpression;

  public PropertiesNoCompatibilityResolver(Pattern mapPattern, Pattern generalPattern, List<Pattern> singleExpressionPatterns,
                                           Pattern patternWithExpression, Pattern patternWithOnlyExpression) {
    this.mapPattern = mapPattern;
    this.generalPattern = generalPattern;
    this.singleExpressionPatterns = singleExpressionPatterns;
    this.patternWithExpression = patternWithExpression;
    this.patternWithOnlyExpression = patternWithOnlyExpression;
  }

  @Override
  public boolean canResolve(String original) {
    return original != null && (generalPattern.matcher(original).find()
        || mapPattern.matcher(original).matches());
  }

  @Override
  public NoCompatibilityResolverResult resolve(String original, Element element, MigrationReport report, ApplicationModel model,
                                               ExpressionMigrator expressionMigrator) {
    String translatedExpression = original;
    boolean success = true;
    if (model.noCompatibilityMode()) {
      try {
        // in case we match part of the expression as referencing the full map of properties we log a message that the expression needs to be changed
        if (mapPattern.matcher(original).matches()) {
          report.report("noCompatibility.mapPattern", element, element, element.getName());
          success = false;
        }
        translatedExpression = translatePropertyReferences(original, element, report, model.getApplicationGraph());
      } catch (MigrationException e) {
        success = false;
      } catch (Exception e) {
        report.report("noCompatibility.unsupportedProperty", element.getParentElement(), element.getParentElement(),
                      element.getName());
        success = false;
      }
    }

    return new NoCompatibilityResolverResult(translatedExpression, success);
  }

  private String translatePropertyReferences(String expression, Element element,
                                             MigrationReport report, ApplicationGraph applicationGraph)
      throws Exception {
    String elementName = element.getName();
    FlowComponent flowComponent = applicationGraph.findFlowComponent(element);

    Matcher matcher = generalPattern.matcher(expression);
    if (flowComponent != null) {
      try {
        if (matcher.find()) {
          return replaceAllOccurencesOfProperty(element, expression, matcher, flowComponent, report,
                                                getTranslator(applicationGraph));
        } else {
          matcher = patternWithExpression.matcher(expression);
          if (matcher.find()) {
            report.report("noCompatibility.melExpression", element, element, elementName);
            report.melExpressionFailure(expression);
          }
        }
      } catch (MigrationException e) {
        throw e;
      } catch (Exception e) {
        report.report("noCompatibility.unsupportedProperty", element, element, elementName);
        throw new MigrationException(e.getMessage());
      }
    } else {
      report.report("noCompatibility.unsupportedProperty", element, element, elementName);
      throw new MigrationException("There was an issue trying to resolve expression to no compatibility. Application graph is not correctly populated");
    }

    // nothing to translate
    return expression;
  }

  private String replaceAllOccurencesOfProperty(Element element, String content, Matcher outerMatcher,
                                                FlowComponent flowComponent,
                                                MigrationReport report, PropertyTranslator translator)
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
          report.report("noCompatibility.unsupportedProperty", element, element, element.getName());
          failedCompleteTranslation = true;
        }

        String propertyToTranslate = specificPropMatcher.group(1);
        String propertyTranslation = null;
        try {
          List<String> possibleTranslations =
              getPropertyTranslations(flowComponent.getPropertiesMigrationContext(), propertyToTranslate,
                                      translator);
          if (possibleTranslations != null && possibleTranslations.size() > 0) {
            propertyTranslation = possibleTranslations.get(0);
          }

          if (possibleTranslations.size() > 1) {
            report.report("noCompatibility.collidingProperties", element, element, propertyToTranslate);
          }

          if (propertyTranslation == null) {
            report.report("noCompatibility.unsupportedProperty", element, element, element.getName());
            failedCompleteTranslation = true;
          }

          if (propertyTranslation != null) {
            contentTranslation = content.replace(specificPropMatcher.group(0), propertyTranslation);
          }
        } catch (Exception e) {
          report.report("noCompatibility.unsupportedProperty", element, element, element.getName());
          failedCompleteTranslation = true;
        }
      }

      if (failedCompleteTranslation) {
        throw new MigrationException("Failed to translate all occurrences of properties");
      }
    }

    return contentTranslation;
  }

  private boolean containsExpression(String referenceToProperty) {
    return referenceToProperty.matches(patternWithOnlyExpression.pattern());
  }

  protected abstract PropertyTranslator getTranslator(ApplicationGraph graph);

  protected abstract List<String> getPropertyTranslations(PropertiesMigrationContext context, String propertyToTranslate,
                                                          PropertyTranslator translator);
}
