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
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.HTTP_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.validation.ValidationMigration.VALIDATION_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.validation.ValidationMigration.addValidationNamespace;
import static com.mulesoft.tools.migration.library.mule.steps.validation.ValidationPomContribution.addValidationDependency;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;

/**
 * Migrates the transformers of the http transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpTransformers extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri()='" + HTTP_NAMESPACE_URI + "' and (local-name()='body-to-parameter-map-transformer' or "
          + "local-name()='request-wildcard-filter' or "
          + "local-name()='http-response-to-object-transformer' or "
          + "local-name()='http-response-to-string-transformer' or "
          + "local-name()='object-to-http-request-transformer' or "
          + "local-name()='message-to-http-response-transformer' or "
          + "local-name()='body-to-parameter-map-transformer')]";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update HTTP tranformers.";
  }

  public HttpTransformers() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    if ("request-wildcard-filter".equals(object.getName())) {
      addValidationDependency(getApplicationModel().getPomModel().get());
      addValidationNamespace(object.getDocument());

      Element wildcardFilterTryScope = new Element("try", CORE_NAMESPACE);

      Element matchValidator = new Element("matches-regex", VALIDATION_NAMESPACE);

      String regex;
      Element parent = object.getParentElement();
      if ("not-filter".equals(parent.getName()) && parent.getNamespace().equals(CORE_NAMESPACE)) {
        addElementAfter(wildcardFilterTryScope, object.getParentElement());
        object.getParentElement().detach();

        regex = "^(?!" + object.getAttributeValue("pattern").replaceAll("\\*", ".*") + ").*$";
      } else {
        regex = "^" + object.getAttributeValue("pattern").replaceAll("\\*", ".*") + "$";

        addElementAfter(wildcardFilterTryScope, object);
        object.detach();
      }

      matchValidator.setAttribute("value", "#[message.attributes.requestPath]");
      matchValidator.setAttribute("regex", regex);
      matchValidator.removeAttribute("pattern");

      wildcardFilterTryScope.addContent(matchValidator);
      wildcardFilterTryScope.addContent(new Element("error-handler", CORE_NAMESPACE)
          .addContent(new Element("on-error-propagate", CORE_NAMESPACE)
              .addContent(new Element("set-variable", CORE_NAMESPACE)
                  .setAttribute("variableName", "statusCode").setAttribute("value", "406"))
              .setAttribute("type", "MULE:VALIDATION")));
    } else if ("body-to-parameter-map-transformer".equals(object.getName())) {
      Element bodyToParamMap =
          new Element("set-payload", CORE_NAMESPACE).setAttribute("value", "#[output application/java --- payload]");
      if (object.getParentElement() == object.getDocument().getRootElement()) {
        getApplicationModel().getNodes("//mule:transformer[@ref='" + object.getAttributeValue("name") + "']").forEach(t -> {
          addElementAfter(bodyToParamMap, t);
          t.detach();
        });
      } else {
        addElementAfter(bodyToParamMap, object);
      }
      object.detach();
    } else {
      if (object.getAttribute("name") != null) {
        getApplicationModel().getNodes("//mule:transformer[@ref = '" + object.getAttributeValue("name") + "']")
            .forEach(t -> t.detach());
      }
      object.detach();
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
