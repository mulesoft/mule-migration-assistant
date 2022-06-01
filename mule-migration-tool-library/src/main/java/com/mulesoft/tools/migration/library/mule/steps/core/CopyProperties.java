/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.project.model.applicationgraph.SetPropertyProcessor.OUTBOUND_PREFIX;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.*;

import com.mulesoft.tools.migration.project.model.applicationgraph.ApplicationGraph;
import com.mulesoft.tools.migration.project.model.applicationgraph.CopyPropertiesProcessor;
import com.mulesoft.tools.migration.project.model.applicationgraph.PropertyMigrationContext;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.Element;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    ApplicationGraph graph = getApplicationModel().getApplicationGraph();
    if (graph != null) {
      CopyPropertiesProcessor processor =
          (CopyPropertiesProcessor) getApplicationModel().getApplicationGraph().findFlowComponent(element);

      Set<Map.Entry<String, PropertyMigrationContext>> inboundContext =
          processor.getPropertiesMigrationContext().getInboundContext()
              .entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).collect(
                                                                                           Collectors
                                                                                               .toCollection(LinkedHashSet::new));
      report.report("nocompatibility.copyproperties", element, element.getParentElement());
      int copyPropertiesIndex = element.getParentElement().indexOf(element);
      for (Map.Entry<String, PropertyMigrationContext> inbound : inboundContext) {
        Element setVariable = new Element("set-variable", CORE_NAMESPACE)
            .setAttribute("variableName", String.format("vars.%s%s", OUTBOUND_PREFIX, inbound.getKey()))
            .setAttribute("value", expressionMigrator.wrap(processor.getPropertiesMigrationContext()
                .getInboundTranslation(inbound.getKey(), graph.getInboundTranslator(), true)));
        element.getParentElement().addContent(copyPropertiesIndex++, setVariable);
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
