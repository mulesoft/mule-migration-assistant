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

import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getContainerElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateRedeliveryPolicyChildren;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.ArrayList;
import java.util.List;

/**
 * Migration step to update Rollback Exception Strategy
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RollbackExceptionStrategy extends AbstractExceptionsMigrationStep {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("rollback-exception-strategy");

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

    encapsulateException(element);

    boolean hadWhen = element.getAttribute("when") != null;
    migrateWhenExpression(element);

    if (element.getAttribute("maxRedeliveryAttempts") != null) {
      Attribute maxRedelivery = element.getAttribute("maxRedeliveryAttempts");
      maxRedelivery.detach();

      Element flow = getContainerElement(element);
      if (flow != null && !flow.getChildren().isEmpty()) {
        Element source = flow.getChildren().get(0);
        if (source.getAttribute("isMessageSource", Namespace.getNamespace("migration")) != null) {
          Element redelivery = source.getChild("idempotent-redelivery-policy", CORE_NAMESPACE);
          if (redelivery != null) {
            redelivery.setName("redelivery-policy");
            Attribute exprAttr = redelivery.getAttribute("idExpression");

            exprAttr.setValue(getExpressionMigrator().migrateExpression(exprAttr.getValue(), true, redelivery));

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

            migrateRedeliveryPolicyChildren(redelivery, report);
          } else {
            Element redeliveryPolicy = new Element("redelivery-policy");
            redeliveryPolicy.setNamespace(CORE_NAMESPACE);
            redeliveryPolicy.setAttribute("maxRedeliveryCount", maxRedelivery.getValue());

            source.addContent(0, redeliveryPolicy);
          }
        }
      } else {
        report.report("errorHandling.redelivery", element, element);
      }
    }

    if (element.getChild("on-redelivery-attempts-exceeded", element.getNamespace()) != null) {
      Element redeliverySection = element.getChild("on-redelivery-attempts-exceeded", element.getNamespace());
      redeliverySection.detach();

      Element newOnError = new Element("on-error-propagate");
      newOnError.setNamespace(element.getNamespace());
      newOnError.setAttribute("type", "REDELIVERY_EXHAUSTED");

      List<Element> redeliveryElements = new ArrayList<>();

      element.getChildren().forEach(e -> redeliveryElements.add(e.clone()));

      redeliverySection.getChildren().forEach(e -> {
        e.detach();
        redeliveryElements.add(e);
      });

      newOnError.addContent(redeliveryElements);

      element.getParentElement().addContent(newOnError);
      if (!hadWhen) {
        element.detach();
      }
    }

  }

}
