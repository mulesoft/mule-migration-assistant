/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.throttling;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.COM_MULESOFT_ANYPOINT_GROUP_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.FALSE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_PLUGIN_CLASSIFIER;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_THROTTLING_EXTENSION_ARTIFACT_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.TRUE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_CORE_NAMESPACE_NO_PREFIX;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.THROTTLING_MULE_4_NAMESPACE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;

import com.mulesoft.tools.migration.library.gateway.steps.policy.throttling.FixedTimeFrameAlgorithmMigrationStep;
import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;

import java.util.List;

import org.jdom2.Element;
import org.junit.Test;

public class FixedTimeAlgorithmMigrationStepTestCase extends AbstractThrottlingTestCase {

  private static final String FIXED_TIME_FRAME_ALGORITHM_TAG_NAME = "fixed-time-frame-algorithm";

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

  private static final String THROTTLE_TAG_NAME = "throttle";
  private static final String REATTEMPTS_ATTR_NAME = "reattempts";
  private static final String REATTEMPTS_DELAY_ATTR_NAME = "reattemptsDelay";

  @Override
  protected String getPolicyPom() {
    return "rate-limit-pom.xml";
  }

  @Override
  protected void assertConfigAttr(Element config, int rateLimitElements, boolean isRateLimit) {
    if (isRateLimit || rateLimitElements > 1) {
      assertThat(config.getAttributeValue(NAME_ATTR_NAME), is(CONFIG_NAME_ATTR_VALUE_RATE_LIMIT));
      assertThat(config.getAttributeValue(CLUSTERIZABLE_ATTR_NAME), is(TRUE));
    } else {
      assertThat(config.getAttributeValue(NAME_ATTR_NAME), is(CONFIG_NAME_ATTR_VALUE_THROTTLING));
      assertThat(config.getAttributeValue(CLUSTERIZABLE_ATTR_NAME), is(FALSE));
      assertThat(config.getAttributeValue(QUEUING_LIMIT_ATTR_NAME), is(FIVE));
    }
  }

  private void assertTierElement(Element tierElement) {
    assertThat(tierElement, notNullValue());
    assertThat(tierElement.getName(), is(TIER_TAG_NAME));
    assertThat(tierElement.getNamespace(), is(THROTTLING_MULE_4_NAMESPACE));
    assertThat(tierElement.getAttributes().size(), is(2));
    assertThat(tierElement.getAttributeValue(MAXIMUM_REQUESTS_PER_PERIOD_ATTR_NAME), notNullValue());
    assertThat(tierElement.getAttributeValue(TIME_PERIOD_MILLIS_ATTR_NAME), is(TIME_PERIOD_MILLIS_ATTR_VALUE));
  }

  private void assertTiersElement(Element explicitElement, int rateLimitElements, boolean isRateLimit) {
    if (isRateLimit || rateLimitElements > 1) {
      Element keysElement = explicitElement.getChild(KEYS_TAG_NAME, THROTTLING_MULE_4_NAMESPACE);
      assertThat(keysElement, notNullValue());
      Element keyElement = keysElement.getChild(KEY_TAG_NAME, THROTTLING_MULE_4_NAMESPACE);
      assertThat(keyElement, notNullValue());
      Element tiersElement = keyElement.getChild(TIERS_TAG_NAME, THROTTLING_MULE_4_NAMESPACE);
      assertThat(tiersElement, notNullValue());
      assertThat(tiersElement.getContentSize(), is(rateLimitElements));
      List<Element> tierElementList = tiersElement.getChildren();
      assertThat(tierElementList, notNullValue());
      tierElementList.forEach(element -> assertTierElement(element));
    } else {
      Element tiersElement = explicitElement.getChild(TIERS_TAG_NAME, THROTTLING_MULE_4_NAMESPACE);
      assertThat(tiersElement, notNullValue());
      assertThat(tiersElement.getContentSize(), is(1));
      Element tierElement = tiersElement.getChild(TIER_TAG_NAME, THROTTLING_MULE_4_NAMESPACE);
      assertTierElement(tierElement);
    }
  }

  @Override
  protected void assertConfigContentElements(Element tierProviderElement, int rateLimitElements, boolean isRateLimit) {
    Element explicitElement = tierProviderElement.getChild(EXPLICIT_TAG_NAME, THROTTLING_MULE_4_NAMESPACE);
    assertThat(explicitElement, notNullValue());
    assertTiersElement(explicitElement, rateLimitElements, isRateLimit);
  }

  @Override
  protected void assertBeforeElements(Element beforeElement, int rateLimitElements, boolean isRateLimit) {
    if (isRateLimit || rateLimitElements > 1) {
      assertThat(beforeElement, notNullValue());
      assertThat(beforeElement.getName(), is(RATE_LIMIT_TAG_NAME));
      assertThat(beforeElement.getNamespace(), is(THROTTLING_MULE_4_NAMESPACE));
      assertThat(beforeElement.getAttributes().size(), is(3));
      assertThat(beforeElement.getAttributeValue(ID), is(ID_ATTR_VALUE_RATE_LIMIT));
      assertThat(beforeElement.getAttributeValue(CONFIG_REF_ATTR_NAME), is(CONFIG_NAME_ATTR_VALUE_RATE_LIMIT));
      assertThat(beforeElement.getAttributeValue(TARGET_ATTR_NAME), is(TARGET_ATTR_VALUE));
    } else {
      assertThat(beforeElement, notNullValue());
      assertThat(beforeElement.getName(), is(THROTTLE_TAG_NAME));
      assertThat(beforeElement.getNamespace(), is(THROTTLING_MULE_4_NAMESPACE));
      assertThat(beforeElement.getAttributes().size(), is(5));
      assertThat(beforeElement.getAttributeValue(ID), is(ID_ATTR_VALUE_THROTTLING));
      assertThat(beforeElement.getAttributeValue(CONFIG_REF_ATTR_NAME), is(CONFIG_NAME_ATTR_VALUE_THROTTLING));
      assertThat(beforeElement.getAttributeValue(TARGET_ATTR_NAME), is(TARGET_ATTR_VALUE));
      assertThat(beforeElement.getAttributeValue(REATTEMPTS_ATTR_NAME), is(DELAY_ATTEMPTS_ATTR_VALUE));
      assertThat(beforeElement.getAttributeValue(REATTEMPTS_DELAY_ATTR_NAME), is(DELAY_TIME_IN_MILLIS_ATTR_VALUE));
    }
  }

  @Override
  protected void assertErrorHandlerElement(Element errorHandlerElement, int rateLimitElements, boolean isRateLimit) {
    assertThat(errorHandlerElement, notNullValue());
    assertThat(errorHandlerElement.getName(), is(ERROR_HANDLER_TAG_NAME));
    assertThat(errorHandlerElement.getNamespace(), is(MULE_4_CORE_NAMESPACE_NO_PREFIX));
    assertThat(errorHandlerElement.getContentSize(), is(2));
    assertQuotaExceededOnErrorContinueElement((Element) errorHandlerElement.getContent(0), rateLimitElements, isRateLimit);
    assertOnErrorPropagateElement((Element) errorHandlerElement.getContent(1));
  }

  @Test
  public void oneRateLimitElement() {
    FixedTimeFrameAlgorithmMigrationStep step = new FixedTimeFrameAlgorithmMigrationStep();
    step.setApplicationModel(appModel);
    Element element = createPolicy(1, FIXED_TIME_FRAME_ALGORITHM_TAG_NAME, true);

    step.execute(element, reportMock);

    assertConfigElement(element, 1, true);
    assertOperationElements(element, 1, true);
    assertNamespaces(element);
    assertPolicyName(element);
  }

  @Test
  public void multipleRateLimitElements() {
    FixedTimeFrameAlgorithmMigrationStep step = new FixedTimeFrameAlgorithmMigrationStep();
    step.setApplicationModel(appModel);
    Element element = createPolicy(2, FIXED_TIME_FRAME_ALGORITHM_TAG_NAME, true);

    step.execute(element, reportMock);

    assertConfigElement(element, 2, true);
    assertOperationElements(element, 2, true);
    assertNamespaces(element);
    assertPolicyName(element);
  }

  @Test
  public void oneRateLimitElementSpikeControl() {
    FixedTimeFrameAlgorithmMigrationStep step = new FixedTimeFrameAlgorithmMigrationStep();
    step.setApplicationModel(appModel);
    Element element = createPolicy(1, FIXED_TIME_FRAME_ALGORITHM_TAG_NAME, false);

    step.execute(element, reportMock);

    assertConfigElement(element, 1, false);
    assertOperationElements(element, 1, false);
    assertNamespaces(element);
    assertPolicyName(element);
  }

  @Test
  public void multipleRateLimitElementsSpikeControl() {
    FixedTimeFrameAlgorithmMigrationStep step = new FixedTimeFrameAlgorithmMigrationStep();
    step.setApplicationModel(appModel);
    Element element = createPolicy(2, FIXED_TIME_FRAME_ALGORITHM_TAG_NAME, false);

    step.execute(element, reportMock);

    assertConfigElement(element, 2, false);
    assertOperationElements(element, 2, false);
    assertNamespaces(element);
    verify(reportMock).report("throttling.throttlingMultipleTiersNotSupported", element, element);
    assertPolicyName(element);
  }

  @Test
  public void rateLimitPomContributionTest() throws Exception {
    FixedTimeFrameAlgorithmMigrationStep step = new FixedTimeFrameAlgorithmMigrationStep();
    step.setApplicationModel(appModel);
    Element element = createPolicy(1, FIXED_TIME_FRAME_ALGORITHM_TAG_NAME, false);

    step.execute(element, reportMock);

    PomModel pm = appModel.getPomModel().get();

    assertThat(pm.getDependencies().size(), is(2));
    Dependency policyTransformExtensionDependency = pm.getDependencies().get(1);
    assertThat(policyTransformExtensionDependency.getGroupId(), is(COM_MULESOFT_ANYPOINT_GROUP_ID));
    assertThat(policyTransformExtensionDependency.getArtifactId(), is(MULE_THROTTLING_EXTENSION_ARTIFACT_ID));
    assertThat(policyTransformExtensionDependency.getVersion(), is(notNullValue()));
    assertThat(policyTransformExtensionDependency.getClassifier(), is(MULE_PLUGIN_CLASSIFIER));
  }
}
