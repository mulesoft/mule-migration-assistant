/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
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

import com.mulesoft.tools.migration.library.gateway.steps.policy.throttling.DiscardResponseTagMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;

public class DiscardResponseTagMigrationStepTestCase {

  private static final String DISCARD_RESPONSE_TAG_NAME = "discard-response";

  private Element getTestElement() {
    Element discardResponseElement = new Element(DISCARD_RESPONSE_TAG_NAME, THROTTLING_GW_MULE_3_NAMESPACE);
    new Document().setRootElement(new Element(POLICY_TAG_NAME, MULE_3_POLICY_NAMESPACE)
        .addContent(discardResponseElement));
    return discardResponseElement;
  }

  @Test
  public void migrateDiscardResponseTag() {
    final DiscardResponseTagMigrationStep step = new DiscardResponseTagMigrationStep();
    Element element = getTestElement();

    step.execute(element, mock(MigrationReport.class));

    assertThat(element.getParent(), nullValue());
    assertThat(element.getContentSize(), is(0));
  }

}
