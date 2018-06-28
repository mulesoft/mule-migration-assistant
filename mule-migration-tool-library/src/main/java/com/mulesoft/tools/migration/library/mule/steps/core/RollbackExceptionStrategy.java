/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.Attribute;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.createErrorHandlerParent;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getElementParentFlow;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.isTopLevelElement;

/**
 * Migration step to update Rollback Exception Strategy
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RollbackExceptionStrategy extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = "//*[local-name()='rollback-exception-strategy']";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update references to Rollback Exception Strategy.";
  }

  public RollbackExceptionStrategy() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    changeNodeName("", "on-error-propagate")
        .apply(element);

    if (!element.getParentElement().getName().equals("error-handler") || isTopLevelElement(element)) {
      createErrorHandlerParent(element);
    }

    if (element.getAttribute("when") != null) {
      Attribute whenCondition = element.getAttribute("when");
      whenCondition.setValue(getExpressionMigrator().migrateExpression(whenCondition.getValue(), true, element));
    }

    if (element.getAttribute("maxRedeliveryAttempts") != null) {
      Attribute maxRedelivery = element.getAttribute("maxRedeliveryAttempts");
      maxRedelivery.detach();

      Element flow = getElementParentFlow(element);
      if (flow != null && !flow.getChildren().isEmpty()) {
        Element source = flow.getChildren().get(0);
        if (source.getAttribute("isMessageSource") != null) {
          Element redelivery = source.getChild("idempotent-redelivery-policy", CORE_NAMESPACE);
          if (redelivery != null) {
            redelivery.setName("redelivery-policy");
            Attribute exprAttr = redelivery.getAttribute("idExpression");

            // TODO MMT-128
            exprAttr.setValue(exprAttr.getValue().replaceAll("#\\[header\\:inbound\\:originalFilename\\]", "#[attributes.name]"));

            Attribute maxRedeliveryCountAtt = redelivery.getAttribute("maxRedeliveryCount");
            if (maxRedeliveryCountAtt != null) {
              maxRedeliveryCountAtt.setValue(maxRedelivery.getValue());
            } else {
              redelivery.setAttribute("maxRedeliveryCount", maxRedelivery.getValue());
            }
            if (getExpressionMigrator().isWrapped(exprAttr.getValue())) {
              exprAttr.setValue(getExpressionMigrator()
                  .wrap(getExpressionMigrator().migrateExpression(exprAttr.getValue(), true, element)));
            }
          } else {
            Element redeliveryPolicy = new Element("redelivery-policy");
            redeliveryPolicy.setNamespace(CORE_NAMESPACE);
            redeliveryPolicy.setAttribute("maxRedeliveryCount", maxRedelivery.getValue());

            source.addContent(0, redeliveryPolicy);
          }
        }
      } else {
        report
            .report(ERROR, element, element,
                    "maxRedelivery is not supported anymore. A <redelivery-policy> element must be created on the message source.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-core-exception-strategies#with-redelivery");
      }
    }

    if (element.getChild("on-redelivery-attempts-exceeded", element.getNamespace()) != null) {
      Element redeliverySection = element.getChild("on-redelivery-attempts-exceeded", element.getNamespace());
      redeliverySection.detach();

      Element newOnError = new Element("on-error-propagate");
      newOnError.setNamespace(element.getNamespace());
      newOnError.setAttribute("type", "REDELIVERY_EXHAUSTED");

      List<Element> redeliveryElements = new ArrayList<>();
      redeliveryElements.addAll(redeliverySection.getChildren());
      redeliverySection.getChildren().forEach(redeliverySection::removeContent);

      newOnError.addContent(redeliveryElements);

      element.getParentElement().addContent(newOnError);
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
