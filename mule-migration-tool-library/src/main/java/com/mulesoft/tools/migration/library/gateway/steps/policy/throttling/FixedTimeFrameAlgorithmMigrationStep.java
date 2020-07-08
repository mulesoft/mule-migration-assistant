/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.throttling;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_CORE_NAMESPACE_NO_PREFIX;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_POLICY_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.THROTTLING_GW_MULE_3_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.THROTTLING_MULE_4_NAMESPACE;

import com.mulesoft.tools.migration.library.gateway.steps.policy.utils.QuotaExceededOnErrorContinueElementWriter;
import com.mulesoft.tools.migration.library.gateway.steps.policy.utils.QuotaExceededQueuingLimitReachedOnErrorContinueElementWriter;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.List;

import org.jdom2.Content;
import org.jdom2.Element;

/**
 * Migrate fixed-time-frame-algorithm element
 *
 * @author Mulesoft Inc.
 */
public class FixedTimeFrameAlgorithmMigrationStep extends AbstractThrottlingMigrationStep {

  private static final String FIXED_TIME_FRAME_ALGORITHM_TAG_NAME = "fixed-time-frame-algorithm";

  private static final String DELAY_TIME_IN_MILLIS_ATTR_NAME = "delayTimeInMillis";
  private static final String DELAY_ATTEMPTS_ATTR_NAME = "delayAttempts";

  private static final String CONFIG_NAME_ATTR_VALUE_RATE_LIMIT = "rateLimitConfig";
  private static final String CONFIG_NAME_ATTR_VALUE_THROTTLING = "throttlingConfig";

  private static final String QUEUING_LIMIT_ATTR_NAME = "queuingLimit";
  private static final String FIVE = "5";
  private static final String EXPLICIT_TAG_NAME = "explicit";
  private static final String KEYS_TAG_NAME = "keys";
  private static final String KEY_TAG_NAME = "key";
  private static final String TIERS_TAG_NAME = "tiers";
  private static final String TIER_TAG_NAME = "tier";

  private static final String ID_ATTR_VALUE_RATE_LIMIT = "{{policyId}}-rate-limit";
  private static final String ID_ATTR_VALUE_THROTTLING = "{{policyId}}-throttle";

  private static final String REATTEMPTS_ATTR_NAME = "reattempts";
  private static final String REATTEMPTS_DELAY_ATTR_NAME = "reattemptsDelay";

  public FixedTimeFrameAlgorithmMigrationStep() {
    super(THROTTLING_GW_MULE_3_NAMESPACE, FIXED_TIME_FRAME_ALGORITHM_TAG_NAME);
  }

  private boolean isRateLimitMigration(Element fixedTimeFrameAlgorithmElement, MigrationReport migrationReport) {
    Element parentElement = fixedTimeFrameAlgorithmElement.getParentElement();
    Element discardResponseElement = parentElement.getChild(DISCARD_RESPONSE_TAG_NAME, THROTTLING_GW_MULE_3_NAMESPACE);
    if (discardResponseElement == null) {
      Element delayResponseElement = parentElement.getChild(DELAY_RESPONSE_TAG_NAME, THROTTLING_GW_MULE_3_NAMESPACE);
      if (delayResponseElement != null
          && fixedTimeFrameAlgorithmElement.getChildren(RATE_LIMIT_TAG_NAME, THROTTLING_GW_MULE_3_NAMESPACE).size() > 1) {
        migrationReport.report("throttling.throttlingMultipleTiersNotSupported", fixedTimeFrameAlgorithmElement,
                               fixedTimeFrameAlgorithmElement);
        return true;
      }
      return false;
    }
    return true;
  }

  private Element getTiersElement(Element fixedTimeFrameAlgorithmElement) {
    Element tiersElement = new Element(TIERS_TAG_NAME, THROTTLING_MULE_4_NAMESPACE);
    detachContent(fixedTimeFrameAlgorithmElement.getContent()).forEach(c -> {
      if (c instanceof Element && ((Element) c).getName().equals(RATE_LIMIT_TAG_NAME)) {
        Element rateLimitElement = (Element) c;
        rateLimitElement.setName(TIER_TAG_NAME);
        rateLimitElement.setNamespace(THROTTLING_MULE_4_NAMESPACE);
      }
      tiersElement.addContent(c);
    });
    return tiersElement;
  }

  private boolean configElementAttributesAreCorrect(Element configElement, boolean isRateLimitMigration) {
    if (isRateLimitMigration) {
      return configElement.getAttributeValue(NAME_ATTR_NAME).equals(CONFIG_NAME_ATTR_VALUE_RATE_LIMIT)
          && configElement.getAttributeValue(CLUSTERIZABLE_ATTR_NAME).equals(TRUE);
    }
    return configElement.getAttributeValue(NAME_ATTR_NAME).equals(CONFIG_NAME_ATTR_VALUE_THROTTLING)
        && configElement.getAttributeValue(CLUSTERIZABLE_ATTR_NAME).equals(FALSE)
        && configElement.getAttributeValue(QUEUING_LIMIT_ATTR_NAME).equals(FIVE);
  }

  private boolean configElementExists(Element rootElement, boolean isRateLimitMigration) {
    List<Element> configElements = rootElement.getChildren(CONFIG_TAG_NAME, THROTTLING_MULE_4_NAMESPACE);
    return configElements.stream().filter(configElement -> {
      if (configElement != null && configElementAttributesAreCorrect(configElement, isRateLimitMigration)) {
        Element tierProviderElement = configElement.getChild(TIER_PROVIDER_TAG_NAME, THROTTLING_MULE_4_NAMESPACE);
        if (tierProviderElement != null) {
          Element explicitElement = tierProviderElement.getChild(EXPLICIT_TAG_NAME, THROTTLING_MULE_4_NAMESPACE);
          if (explicitElement != null) {
            return explicitElement.getChild(isRateLimitMigration ? KEYS_TAG_NAME : TIERS_TAG_NAME,
                                            THROTTLING_MULE_4_NAMESPACE) != null;
          }
        }
      }
      return false;
    }).findAny().orElse(null) != null;
  }

  private void addConfigElement(Element fixedTimeFrameAlgorithmElement, boolean isRateLimitMigration) {
    Element rootElement = getRootElement(fixedTimeFrameAlgorithmElement);
    if (!configElementExists(rootElement, isRateLimitMigration)) {
      Element explicitElement = new Element(EXPLICIT_TAG_NAME, THROTTLING_MULE_4_NAMESPACE);
      Element configElement = new Element(CONFIG_TAG_NAME, THROTTLING_MULE_4_NAMESPACE)
          .setAttribute(NAME_ATTR_NAME,
                        isRateLimitMigration ? CONFIG_NAME_ATTR_VALUE_RATE_LIMIT : CONFIG_NAME_ATTR_VALUE_THROTTLING)
          .setAttribute(CLUSTERIZABLE_ATTR_NAME, isRateLimitMigration ? TRUE : FALSE)
          .addContent(new Element(TIER_PROVIDER_TAG_NAME, THROTTLING_MULE_4_NAMESPACE).addContent(explicitElement));
      if (isRateLimitMigration) {
        explicitElement.addContent(
                                   new Element(KEYS_TAG_NAME, THROTTLING_MULE_4_NAMESPACE).addContent(
                                                                                                      new Element(KEY_TAG_NAME,
                                                                                                                  THROTTLING_MULE_4_NAMESPACE)
                                                                                                                      .addContent(getTiersElement(fixedTimeFrameAlgorithmElement))));
      } else {
        configElement.setAttribute(QUEUING_LIMIT_ATTR_NAME, FIVE);
        explicitElement.addContent(getTiersElement(fixedTimeFrameAlgorithmElement));
      }
      rootElement.addContent(configElement);
    }
  }

  private Element getOperationElement(Element fixedTimeFrameAlgorithmElement, boolean isRateLimitMigration) {
    Element element;
    if (isRateLimitMigration) {
      element = new Element(RATE_LIMIT_TAG_NAME, THROTTLING_MULE_4_NAMESPACE);
      element.setAttribute(ID, ID_ATTR_VALUE_RATE_LIMIT);
      element.setAttribute(CONFIG_REF_ATTR_NAME, CONFIG_NAME_ATTR_VALUE_RATE_LIMIT);
    } else {
      element = new Element(THROTTLE_TAG_NAME, THROTTLING_MULE_4_NAMESPACE);
      Element delayResponseElement =
          fixedTimeFrameAlgorithmElement.getParentElement().getChild(DELAY_RESPONSE_TAG_NAME, THROTTLING_GW_MULE_3_NAMESPACE);
      element.setAttribute(REATTEMPTS_ATTR_NAME, delayResponseElement.getAttributeValue(DELAY_ATTEMPTS_ATTR_NAME));
      element.setAttribute(REATTEMPTS_DELAY_ATTR_NAME, delayResponseElement.getAttributeValue(DELAY_TIME_IN_MILLIS_ATTR_NAME));
      element.setAttribute(ID, ID_ATTR_VALUE_THROTTLING);
      element.setAttribute(CONFIG_REF_ATTR_NAME, CONFIG_NAME_ATTR_VALUE_THROTTLING);
    }
    element.setAttribute(TARGET_ATTR_NAME, TARGET_ATTR_VALUE);
    return element;
  }

  private void addOnErrorContinueElement(Element errorHandlerElement, boolean isRateLimitMigration) {
    if (isRateLimitMigration) {
      new QuotaExceededOnErrorContinueElementWriter().create(errorHandlerElement, true);
    } else {
      new QuotaExceededQueuingLimitReachedOnErrorContinueElementWriter().create(errorHandlerElement, true);
    }
  }

  private void completeTryElementWithOperationContent(Element tryElement, Element fixedTimeFrameAlgorithmElement,
                                                      boolean isRateLimitMigration) {
    tryElement.addContent(0, getOperationElement(fixedTimeFrameAlgorithmElement, isRateLimitMigration));
    addAddHeadersElement(tryElement);
    Element errorHandlerElement = tryElement.getChild(ERROR_HANDLER_TAG_NAME, MULE_4_POLICY_NAMESPACE);
    if (errorHandlerElement == null) {
      errorHandlerElement = new Element(ERROR_HANDLER_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
      addOnErrorContinueElement(errorHandlerElement, isRateLimitMigration);
      errorHandlerElement.addContent(getOnErrorPropagateElement());
      tryElement.addContent(errorHandlerElement);
    } else {
      addOnErrorContinueElement(errorHandlerElement, isRateLimitMigration);
      addOnErrorPropagateElement(errorHandlerElement);
    }
  }

  private void addOperationElements(Element fixedTimeFrameAlgorithmElement, boolean isRateLimitMigration,
                                    MigrationReport migrationReport) {
    Element source = setUpHttpPolicy(fixedTimeFrameAlgorithmElement, false, migrationReport);
    Element tryElement = source.getChild(TRY_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
    if (tryElement == null) {
      tryElement = new Element(TRY_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
      final List<Content> sourceCloneContentList = detachContent(source.getContent());
      source.addContent(tryElement);
      sourceCloneContentList.forEach(tryElement::addContent);
    }
    completeTryElementWithOperationContent(tryElement, fixedTimeFrameAlgorithmElement, isRateLimitMigration);
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    boolean isRateLimitMigration = isRateLimitMigration(element, migrationReport);
    addConfigElement(element, isRateLimitMigration);
    addOperationElements(element, isRateLimitMigration, migrationReport);
    addNamespaceDeclarations(element);
    new ThrottlingPomContributionMigrationStep(false).execute(getApplicationModel().getPomModel().get(), migrationReport);
  }
}
