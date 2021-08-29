/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.throttling;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.BEFORE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.POLICY_ID_ATTR_VALUE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_3_POLICY_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.THROTTLING_GW_MULE_3_NAMESPACE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.library.gateway.steps.policy.throttling.ThrottleTagMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;

public class ThrottleTagMigrationStepTestCase {

  private static final String THROTTLE_TAG_NAME = "throttle";

  private static final String THROTTLING_POLICY_REF_ATTR_NAME = "throttling-policy-ref";

  private Element getTestElement() {
    Element delayResponseElement = new Element(THROTTLE_TAG_NAME, THROTTLING_GW_MULE_3_NAMESPACE)
        .setAttribute(THROTTLING_POLICY_REF_ATTR_NAME, POLICY_ID_ATTR_VALUE);
    new Document().setRootElement(new Element(BEFORE_TAG_NAME, MULE_3_POLICY_NAMESPACE)
        .addContent(delayResponseElement));
    return delayResponseElement;
  }

  @Test
  public void migrateThrottleTag() {
    final ThrottleTagMigrationStep step = new ThrottleTagMigrationStep();
    Element element = getTestElement();

    step.execute(element, mock(MigrationReport.class));

    assertThat(element.getParent(), nullValue());
    assertThat(element.getContentSize(), is(0));
  }
}
