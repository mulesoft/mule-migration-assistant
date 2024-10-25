/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.throttling;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.API_PLATFORM_GW_MULE_3_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_CORE_NAMESPACE_NO_PREFIX;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_POLICY_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.THROTTLING_GW_MULE_3_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.THROTTLING_MULE_4_NAMESPACE;

import com.mulesoft.tools.migration.library.gateway.steps.policy.utils.ForbiddenClientOnErrorContinueElementWriter;
import com.mulesoft.tools.migration.library.gateway.steps.policy.utils.QuotaExceededOnErrorContinueElementWriter;
import com.mulesoft.tools.migration.library.gateway.steps.policy.utils.UnknownApiOnErrorContinueElementWriter;
import com.mulesoft.tools.migration.library.gateway.steps.policy.PolicyMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.List;

import org.jdom2.Content;
import org.jdom2.Element;

/**
 * Migrate sla elements
 *
 * @author Mulesoft Inc.
 */
public class SlaBasedAlgorithmMigrationStep extends AbstractThrottlingMigrationStep {

  private static final String SLA_BASED_ALGORITHM = "sla-based-algorithm";

  private static final String CONFIG_NAME_ATTR_VALUE_RATE_LIMIT_SLA = "rateLimitSlaConfig";
  private static final String VALIDATE_CLIENT_TAG_NAME = "validate-client";
  private static final String API_ID_ATTR_NAME = "apiId";
  private static final String API_ID_ATTR_VALUE = "${apiId}";

  private static final String ID_ATTR_VALUE_RATE_LIMIT_SLA = "{{policyId}}-rate-limit-sla";
  private static final String CLIENT_ID_ATTR_NAME = "clientId";
  private static final String CLIENT_SECRET_ATTR_NAME = "clientSecret";
  private static final String CLIENT_ID_EXPRESSION_ATTR_NAME = "clientIdExpression";
  private static final String CLIENT_SECRET_EXPRESSION_ATTR_NAME = "clientSecretExpression";

  public SlaBasedAlgorithmMigrationStep() {
    super(API_PLATFORM_GW_MULE_3_NAMESPACE, SLA_BASED_ALGORITHM);
  }

  private void checkThrottlingSLAPolicy(Element slaBasedAlgorithmElement, MigrationReport migrationReport) {
    Element parentElement = slaBasedAlgorithmElement.getParentElement();
    if (parentElement.getChild(DELAY_RESPONSE_TAG_NAME, THROTTLING_GW_MULE_3_NAMESPACE) != null) {
      migrationReport.report("throttling.throttlingSLANotSupported", parentElement, parentElement);
    }
  }

  private boolean configElementExists(Element rootElement) {
    List<Element> configElements = rootElement.getChildren(CONFIG_TAG_NAME, THROTTLING_MULE_4_NAMESPACE);
    return configElements.stream().filter(configElement -> {
      if (configElement != null && configElement.getAttributeValue(NAME_ATTR_NAME).equals(CONFIG_NAME_ATTR_VALUE_RATE_LIMIT_SLA)
          && configElement.getAttributeValue(CLUSTERIZABLE_ATTR_NAME).equals(TRUE)) {
        Element tierProviderElement = configElement.getChild(TIER_PROVIDER_TAG_NAME, THROTTLING_MULE_4_NAMESPACE);
        if (tierProviderElement != null) {
          Element validateClientElement = tierProviderElement.getChild(VALIDATE_CLIENT_TAG_NAME, THROTTLING_MULE_4_NAMESPACE);
          return validateClientElement != null
              && validateClientElement.getAttributeValue(API_ID_ATTR_NAME).equals(API_ID_ATTR_VALUE);
        }
      }
      return false;
    }).findAny().orElse(null) != null;
  }

  private void addConfigElement(Element slaBasedAlgorithmAlgorithmElement) {
    Element rootElement = getRootElement(slaBasedAlgorithmAlgorithmElement);
    if (!configElementExists(rootElement)) {
      rootElement.addContent(new Element(CONFIG_TAG_NAME, THROTTLING_MULE_4_NAMESPACE)
          .setAttribute(NAME_ATTR_NAME, CONFIG_NAME_ATTR_VALUE_RATE_LIMIT_SLA)
          .setAttribute(CLUSTERIZABLE_ATTR_NAME, TRUE)
          .addContent(new Element(TIER_PROVIDER_TAG_NAME, THROTTLING_MULE_4_NAMESPACE)
              .addContent(new Element(VALIDATE_CLIENT_TAG_NAME, THROTTLING_MULE_4_NAMESPACE).setAttribute(API_ID_ATTR_NAME,
                                                                                                          API_ID_ATTR_VALUE))));
    }
  }

  private Element getOperationElement(Element slaBasedAlgorithmAlgorithmElement) {
    return new Element(RATE_LIMIT_TAG_NAME, THROTTLING_MULE_4_NAMESPACE)
        .setAttribute(PolicyMigrationStep.ID, ID_ATTR_VALUE_RATE_LIMIT_SLA)
        .setAttribute(CONFIG_REF_ATTR_NAME, CONFIG_NAME_ATTR_VALUE_RATE_LIMIT_SLA)
        .setAttribute(TARGET_ATTR_NAME, TARGET_ATTR_VALUE)
        .setAttribute(CLIENT_ID_ATTR_NAME, slaBasedAlgorithmAlgorithmElement.getAttributeValue(CLIENT_ID_EXPRESSION_ATTR_NAME))
        .setAttribute(CLIENT_SECRET_ATTR_NAME,
                      slaBasedAlgorithmAlgorithmElement.getAttributeValue(CLIENT_SECRET_EXPRESSION_ATTR_NAME));
  }

  private void addOnErrorContinueElements(Element errorHandlerElement) {
    new QuotaExceededOnErrorContinueElementWriter().create(errorHandlerElement, true);
    new ForbiddenClientOnErrorContinueElementWriter().create(errorHandlerElement, true);
    new UnknownApiOnErrorContinueElementWriter().create(errorHandlerElement, true);
  }

  private void completeTryElementWithOperationContent(Element tryElement, Element slaBasedAlgorithmAlgorithmElement) {
    tryElement.addContent(0, getOperationElement(slaBasedAlgorithmAlgorithmElement));
    addAddHeadersElement(tryElement);
    Element errorHandlerElement = tryElement.getChild(PolicyMigrationStep.ERROR_HANDLER_TAG_NAME, MULE_4_POLICY_NAMESPACE);
    if (errorHandlerElement == null) {
      errorHandlerElement = new Element(PolicyMigrationStep.ERROR_HANDLER_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
      addOnErrorContinueElements(errorHandlerElement);
      errorHandlerElement.addContent(getOnErrorPropagateElement());
      tryElement.addContent(errorHandlerElement);
    } else {
      addOnErrorContinueElements(errorHandlerElement);
      addOnErrorPropagateElement(errorHandlerElement);
    }
  }

  private void addOperationElements(Element slaBasedAlgorithmAlgorithmElement, MigrationReport migrationReport) {
    Element source = setUpHttpPolicy(slaBasedAlgorithmAlgorithmElement, false, migrationReport);
    Element tryElement = source.getChild(PolicyMigrationStep.TRY_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
    if (tryElement == null) {
      tryElement = new Element(PolicyMigrationStep.TRY_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
      final List<Content> sourceCloneContentList = detachContent(source.getContent());
      source.addContent(tryElement);
      sourceCloneContentList.forEach(tryElement::addContent);
    }
    completeTryElementWithOperationContent(tryElement, slaBasedAlgorithmAlgorithmElement);
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    checkThrottlingSLAPolicy(element, migrationReport);
    addConfigElement(element);
    addOperationElements(element, migrationReport);
    addNamespaceDeclarations(element);
    new ThrottlingPomContributionMigrationStep().execute(getApplicationModel().getPomModel().get(), migrationReport);
  }
}
