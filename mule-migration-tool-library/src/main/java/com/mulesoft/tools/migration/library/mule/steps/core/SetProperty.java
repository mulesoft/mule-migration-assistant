/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeAttribute;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;
import static com.mulesoft.tools.migration.project.model.applicationgraph.SetPropertyProcessor.OUTBOUND_PREFIX;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addCompatibilityNamespace;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateExpression;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.mulesoft.tools.migration.project.model.applicationgraph.RemovePropertyProcessor;
import com.mulesoft.tools.migration.project.model.applicationgraph.SetPropertyProcessor;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;

/**
 * Migrate Set Property to the compatibility plugin
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SetProperty extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = "//mule:set-property";
  private static final String SET_VARIABLE = "set-variable";
  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update Set Property namespace to compatibility.";
  }

  public SetProperty() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(COMPATIBILITY_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    if (getApplicationModel().getApplicationGraph() != null) {
      String propertyName = element.getAttributeValue("propertyName");

      SetPropertyProcessor processor = (SetPropertyProcessor) getApplicationModel()
          .getApplicationGraph().findFlowComponent(element);
      String variableNameTranslation = processor.getPropertiesMigrationContext().getOutboundContext()
          .get(propertyName).getTranslation();
      if (variableNameTranslation != null) {
        variableNameTranslation = variableNameTranslation.replace("vars.", "");
        changeNodeName("", "set-variable")
            .andThen(changeAttribute("propertyName", of("variableName"), of(variableNameTranslation)))
            .apply(element);
      }
      migrateExpression(element.getAttribute("value"), getExpressionMigrator());
    } else {
      migrateExpression(element.getAttribute("value"), getExpressionMigrator());
      addCompatibilityNamespace(element.getDocument());
      report.report("message.outboundProperties", element, element);
      element.setNamespace(COMPATIBILITY_NAMESPACE);
    }
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
