/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addCompatibilityNamespace;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getContainerElement;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.StringUtils.substring;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Migrate enricher scope
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Enricher extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  private static final String ATTACHMENT_ENRICHMENT_REGEX =
      "\\#\\[mel:message\\.outboundAttachments\\['([^']+)'\\] = new DataHandler\\(\\$, 'text\\/plain'\\)]";
  private static final Pattern ATTACHEMNT_ENRICHMENT_PATTERN = compile(ATTACHMENT_ENRICHMENT_REGEX);
  public static final String XPATH_SELECTOR = "//mule:enricher";

  private final Map<String, AtomicInteger> enricherSubFlowIndex = new HashMap<>();

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Migrate enricher scope";
  }

  public Enricher() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    Element flow = getContainerElement(element);
    String subFlowName = getSubFlowName(flow);

    Element flowRef = new Element("flow-ref", CORE_NAMESPACE).setAttribute("name", subFlowName);
    addElementAfter(flowRef, element);

    String target = element.getAttributeValue("target");
    String source = element.getAttributeValue("source");

    if (target == null && source == null) {
      Element firstEnrich = element.getChild("enrich", CORE_NAMESPACE);
      target = firstEnrich.getAttributeValue("target");
      source = firstEnrich.getAttributeValue("source");
      firstEnrich.detach();
    }
    if (element.removeChildren("enrich", CORE_NAMESPACE)) {
      report.report("enricher.multipleEnrichments", element, flowRef);
    }

    element.detach();
    Element subFlow = new Element("sub-flow", CORE_NAMESPACE).setAttribute("name", subFlowName);

    if (element.getChild("processor-chain", CORE_NAMESPACE) != null) {
      subFlow.addContent(element.getChild("processor-chain", CORE_NAMESPACE).cloneContent());
    } else {
      subFlow.addContent(element.cloneContent());
    }

    if (flow != null) {
      addElementAfter(subFlow, flow);
    } else {
      addTopLevelElement(subFlow, flowRef.getDocument());
    }

    if (target != null) {
      String migratedTargetExpr;
      if (target.startsWith("#[message.outboundProperties.")) {
        migratedTargetExpr = substring(target, "#[message.outboundProperties.".length(), -1);
      } else {
        migratedTargetExpr = getExpressionMigrator().migrateExpression(target, true, flowRef, true);
      }

      if (migratedTargetExpr.startsWith("#[vars.")) {
        migratedTargetExpr = substring(migratedTargetExpr, "#[vars.".length(), -1);
      } else if (migratedTargetExpr
          .matches(ATTACHMENT_ENRICHMENT_REGEX)) {
        Matcher matcher = ATTACHEMNT_ENRICHMENT_PATTERN.matcher(migratedTargetExpr);
        matcher.matches();
        migratedTargetExpr = matcher.group(1);
      } else {
        migratedTargetExpr = getExpressionMigrator().unwrap(migratedTargetExpr);

        addCompatibilityNamespace(flowRef.getDocument());
        addElementAfter(new Element("outbound-properties-to-var", COMPATIBILITY_NAMESPACE), flowRef);
        addElementAfter(new Element("set-property", COMPATIBILITY_NAMESPACE)
            .setAttribute("propertyName", migratedTargetExpr)
            .setAttribute("value", "#[vars." + migratedTargetExpr + "]"), flowRef);

        report.report("enricher.multipleEnrichments", element, flowRef);
      }

      flowRef.setAttribute("target", migratedTargetExpr);
    }

    if (source != null) {
      flowRef.setAttribute("targetValue", getExpressionMigrator().migrateExpression(source, true, flowRef));
    }

  }

  private String getSubFlowName(Element flow) {
    String flowName = "MMA_Top_Level";
    if (flow != null) {
      flowName = flow.getAttributeValue("name") != null ? flow.getAttributeValue("name")
          : flow.getParentElement().getName() + StringUtils.capitalize(flow.getName());
    }
    return flowName + "_Enricher_" + enricherSubFlowIndex
        .computeIfAbsent(flowName, k -> new AtomicInteger()).getAndIncrement();
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
