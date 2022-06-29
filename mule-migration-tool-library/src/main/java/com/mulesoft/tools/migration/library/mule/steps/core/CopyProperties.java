/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.project.model.applicationgraph.PropertyTranslator.OUTBOUND_PREFIX;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addCompatibilityNamespace;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import com.mulesoft.tools.migration.project.model.applicationgraph.CopyPropertiesProcessor;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import java.util.List;
import java.util.stream.Collectors;

import org.jdom2.Element;

/**
 * Migrate Copy Properties to the compatibility plugin
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CopyProperties extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("copy-properties");
  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update Copy Properties namespace to compatibility.";
  }

  public CopyProperties() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(COMPATIBILITY_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    if (getApplicationModel().noCompatibilityMode()) {
      CopyPropertiesProcessor processor =
          (CopyPropertiesProcessor) getApplicationModel().getApplicationGraph().findFlowComponent(element);

      report.report("nocompatibility.copyproperties", element, element.getParentElement());
      int copyPropertiesIndex = element.getParentElement().indexOf(element);
      List<String> allInboundKeys = processor.getPropertiesMigrationContext().getAllInboundKeys().stream()
          .sorted().collect(Collectors.toList());
      for (String key : allInboundKeys) {
        List<String> possibleTranslations = processor.getPropertiesMigrationContext().getInboundTranslation(key, true);
        if (!possibleTranslations.isEmpty()) {
          Element setVariable = new Element("set-variable", CORE_NAMESPACE)
              .setAttribute("variableName", OUTBOUND_PREFIX + key)
              .setAttribute("value", expressionMigrator.wrap(possibleTranslations.get(0)));
          element.getParentElement().addContent(copyPropertiesIndex++, setVariable);
          if (possibleTranslations.size() > 1) {
            report.report("nocompatibility.collidingProperties", setVariable, setVariable, setVariable.getName());
          }
        }
      }
      element.detach();
    } else {
      addCompatibilityNamespace(element.getDocument());
      report.report("message.copyProperties", element, element);
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
