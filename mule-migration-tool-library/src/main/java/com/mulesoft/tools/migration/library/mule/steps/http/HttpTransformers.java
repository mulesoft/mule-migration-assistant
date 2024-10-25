/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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
