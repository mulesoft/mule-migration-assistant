/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.throttling;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.POLICY_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_3_POLICY_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.THROTTLING_GW_MULE_3_NAMESPACE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.library.gateway.steps.policy.throttling.DelayResponseTagMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;

public class DelayResponseTagMigrationStepTestCase {

  private static final String DELAY_RESPONSE_TAG_NAME = "delay-response";

  private static final String DELAY_TIME_IN_MILLIS_ATTR_NAME = "delayTimeInMillis";
  private static final String DELAY_TIME_IN_MILLIS_ATTR_VALUE = "{{delayTimeInMillis}}";
  private static final String DELAY_ATTEMPTS_ATTR_NAME = "delayAttempts";
  private static final String DELAY_ATTEMPTS_ATTR_VALUE = "{{delayAttempts}}";

  private Element getTestElement() {
    Element delayResponseElement = new Element(DELAY_RESPONSE_TAG_NAME, THROTTLING_GW_MULE_3_NAMESPACE)
        .setAttribute(DELAY_TIME_IN_MILLIS_ATTR_NAME, DELAY_TIME_IN_MILLIS_ATTR_VALUE)
        .setAttribute(DELAY_ATTEMPTS_ATTR_NAME, DELAY_ATTEMPTS_ATTR_VALUE);
    new Document().setRootElement(new Element(POLICY_TAG_NAME, MULE_3_POLICY_NAMESPACE)
        .addContent(delayResponseElement));
    return delayResponseElement;
  }

  @Test
  public void migrateDelayResponseTag() {
    final DelayResponseTagMigrationStep step = new DelayResponseTagMigrationStep();
    Element element = getTestElement();

    step.execute(element, mock(MigrationReport.class));

    assertThat(element.getParent(), nullValue());
    assertThat(element.getContentSize(), is(0));
  }
}
