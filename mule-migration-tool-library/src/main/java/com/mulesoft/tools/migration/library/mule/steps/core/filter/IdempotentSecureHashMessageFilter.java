/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.filter;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate SecureHash Idempotent Message Filter to Idempotent-Message Validator
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class IdempotentSecureHashMessageFilter extends AbstractFilterMigrator {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("idempotent-secure-hash-message-filter");

  @Override
  public String getDescription() {
    return "Update Idempotent SecureHash Message Filter to Idempotent-Message Validator.";
  }

  public IdempotentSecureHashMessageFilter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    String messageDigestAlgorithm = element.getAttributeValue("messageDigestAlgorithm");
    messageDigestAlgorithm = messageDigestAlgorithm == null ? "SHA-256" : messageDigestAlgorithm;
    element.removeAttribute("messageDigestAlgorithm");

    String idExpression = element.getAttributeValue("idExpression");
    idExpression = idExpression == null ? "correlationId" : getExpressionMigrator().unwrap(idExpression);

    element.setAttribute("idExpression", "%dw 2.0\n" +
        "            output text/plain\n" +
        "            import dw::Crypto\n" +
        "            ---\n" +
        "            Crypto::hashWith(" + idExpression + ", '" + messageDigestAlgorithm + "')");

    element.setName("idempotent-message-validator");
    handleFilter(element);
  }

  @Override
  protected Element resolveValidationHandler(Element errorHandler) {
    return errorHandler.getChildren().stream()
        .filter(c -> "on-error-propagate".equals(c.getName()) && "DUPLICATE_MESSAGE".equals(c.getAttributeValue("type")))
        .findFirst().orElseGet(() -> {
          Element validationHandler = new Element("on-error-propagate", CORE_NAMESPACE)
              .setAttribute("type", "DUPLICATE_MESSAGE")
              .setAttribute("logException", "false");
          errorHandler.addContent(0, validationHandler);
          validationHandler.addContent(new Element("set-variable", CORE_NAMESPACE)
              .setAttribute("variableName", "filtered")
              .setAttribute("value", "#[true]"));
          return validationHandler;
        });
  }

}
