/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

import static com.mulesoft.tools.migration.library.mule.steps.validation.ValidationMigration.VALIDATION_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.validation.ValidationMigration.addValidationNamespace;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getContainerElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getFlowExceptionHandlingElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.removeAttribute;
import static java.lang.Integer.parseInt;

/**
 * Migrate Until Successful
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class UntilSuccessful extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = "//mule:*[local-name()='until-successful']";
  private ExpressionMigrator expressionMigrator;

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }

  @Override
  public String getDescription() {
    return "Migrate Until Successful.";
  }

  public UntilSuccessful() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    List<Element> childs = new ArrayList<>(element.getChildren());
    childs.forEach(c -> {
      if (c.getName().equals("processor-chain")) {
        List<Element> chainNodes = new ArrayList<>(c.getChildren());
        chainNodes.forEach(n -> n.detach());
        element.addContent(element.indexOf(c), chainNodes);
        c.detach();
      } else if (c.getName().equals("threading-profile")) {
        report.report("untilSuccessful.threading", c, element);
        c.detach();
      }
    });

    removeAttribute(element, "objectStore-ref");
    removeAttribute(element, "synchronous");

    if (element.getAttribute("ackExpression") != null) {
      Element setPayload = new Element("set-payload", element.getNamespace());
      setPayload
          .setAttribute("value",
                        getExpressionMigrator().migrateExpression(element.getAttributeValue("ackExpression"), true, element));
      addElementAfter(setPayload, element);
      removeAttribute(element, "ackExpression");
    }

    if (element.getAttribute("secondsBetweenRetries") != null) {
      String retries = element.getAttributeValue("secondsBetweenRetries");
      try {
        retries = String.valueOf(parseInt(retries) * 1000);
      } catch (NumberFormatException nfe) {
        String propertyName = retries.startsWith("${") ? retries.substring(2, retries.length() - 1) : retries;
        report.report("untilSuccessful.secondsBetweenRetries", element, element, propertyName);
      }
      element.setAttribute("millisBetweenRetries", retries);
      removeAttribute(element, "secondsBetweenRetries");
    }

    if (element.getAttribute("deadLetterQueue-ref") != null) {
      Element flow = getContainerElement(element);
      Element errorHandler = getFlowExceptionHandlingElement(flow);

      if (errorHandler == null) {
        errorHandler = new Element("error-handler", element.getNamespace());
        flow.addContent(flow.getContent().size() - 1, errorHandler);
      }

      Element retryExhaustedHandler = new Element("on-error-propagate", element.getNamespace());
      retryExhaustedHandler.setAttribute("type", "RETRY_EXHAUSTED");

      Element outboundEndPoint = new Element("outbound-endpoint", element.getNamespace());
      outboundEndPoint.setAttribute("ref", element.getAttributeValue("deadLetterQueue-ref"));

      retryExhaustedHandler.addContent(outboundEndPoint);
      errorHandler.addContent(retryExhaustedHandler);
      removeAttribute(element, "deadLetterQueue-ref");
    }

    if (element.getAttribute("failureExpression") != null) {
      addValidationNamespace(element.getDocument());
      Element validation = new Element("is-false", VALIDATION_NAMESPACE);
      String expression = element.getAttributeValue("failureExpression");
      validation.setAttribute("expression", getExpressionMigrator().migrateExpression(expression, true, element));
      element.addContent(validation);
      removeAttribute(element, "failureExpression");
    }

  }

}
