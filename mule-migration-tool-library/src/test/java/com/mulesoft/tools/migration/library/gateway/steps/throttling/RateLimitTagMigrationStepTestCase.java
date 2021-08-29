/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.throttling;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.DATA_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.POLICY_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_3_POLICY_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.THROTTLING_GW_MULE_3_NAMESPACE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.library.gateway.steps.policy.throttling.RateLimitTagMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;

public class RateLimitTagMigrationStepTestCase {

  private static final String FIXED_TIME_FRAME_ALGORITHM_TAG_NAME = "fixed-time-frame-algorithm";

  private static final String COLLECTION_TAG_NAME = "collection";
  private static final String SLA_TAG_NAME = "sla";

  private static final String RATE_LIMIT_TAG_NAME = "rate-limit";
  private static final String MAXIMUM_REQUESTS_PER_PERIOD_ATTR_NAME = "maximumRequestsPerPeriod";
  private static final String MAXIMUM_REQUESTS_PER_PERIOD_ATTR_VALUE = "{{maximumRequestsPerPeriod}}";
  private static final String TIME_PERIOD_MILLIS_ATTR_NAME = "timePeriodMillis";
  private static final String TIME_PERIOD_MILLIS_ATTR_VALUE = "{{timePeriodInMilliseconds}}";

  private Element getRateLimitElement() {
    return new Element(RATE_LIMIT_TAG_NAME, THROTTLING_GW_MULE_3_NAMESPACE)
        .setAttribute(MAXIMUM_REQUESTS_PER_PERIOD_ATTR_NAME, MAXIMUM_REQUESTS_PER_PERIOD_ATTR_VALUE)
        .setAttribute(TIME_PERIOD_MILLIS_ATTR_NAME, TIME_PERIOD_MILLIS_ATTR_VALUE);
  }

  private Element getSpikeControlRateLimitElement() {
    Element rateLimitElement = getRateLimitElement();
    new Document().setRootElement(
                                  new Element(POLICY_TAG_NAME, MULE_3_POLICY_NAMESPACE).addContent(
                                                                                                   new Element(POLICY_TAG_NAME,
                                                                                                               THROTTLING_GW_MULE_3_NAMESPACE)
                                                                                                                   .addContent(
                                                                                                                               new Element(FIXED_TIME_FRAME_ALGORITHM_TAG_NAME,
                                                                                                                                           THROTTLING_GW_MULE_3_NAMESPACE)
                                                                                                                                               .addContent(rateLimitElement))));
    return rateLimitElement;
  }

  private Element getRateLimitSLAElement() {
    Element rateLimitElement = getRateLimitElement();
    new Document().setRootElement(
                                  new Element(POLICY_TAG_NAME, MULE_3_POLICY_NAMESPACE).addContent(
                                                                                                   new Element(DATA_TAG_NAME,
                                                                                                               MULE_3_POLICY_NAMESPACE)
                                                                                                                   .addContent(
                                                                                                                               new Element(COLLECTION_TAG_NAME,
                                                                                                                                           MULE_3_POLICY_NAMESPACE)
                                                                                                                                               .addContent(
                                                                                                                                                           new Element(SLA_TAG_NAME,
                                                                                                                                                                       MULE_3_POLICY_NAMESPACE)
                                                                                                                                                                           .addContent(rateLimitElement)))));
    return rateLimitElement;
  }

  @Test
  public void migrateRateLimitTagSpikeControlRateLimit() {
    final RateLimitTagMigrationStep step = new RateLimitTagMigrationStep();
    Element element = getSpikeControlRateLimitElement();

    step.execute(element, mock(MigrationReport.class));

    assertThat(element.getParent(), nullValue());
    assertThat(element.getContentSize(), is(0));
  }

  @Test
  public void migrateRateLimitTagRateLimitSLA() {
    final RateLimitTagMigrationStep step = new RateLimitTagMigrationStep();
    Element element = getRateLimitSLAElement();

    step.execute(element, mock(MigrationReport.class));

    assertThat(element.getParent(), nullValue());
    assertThat(element.getContentSize(), is(0));
  }

}
