/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
      Integer retries = Integer.valueOf(element.getAttributeValue("secondsBetweenRetries")) * 1000;
      element.setAttribute("millisBetweenRetries", retries.toString());
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
