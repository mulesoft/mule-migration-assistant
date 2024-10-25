/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.basicstructure;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.EXECUTE_NEXT_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_ATTRIBUTE_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_ATTRIBUTE_VALUE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.PROXY_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.SOURCE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_POLICY_NAMESPACE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.mulesoft.tools.migration.library.gateway.steps.policy.basicstructure.CleanupAttributesMigrationStep;

import org.jdom2.Element;
import org.junit.Test;

public class CleanupAttributesMigrationStepTestCase extends BasicPolicyStructureMigrationTestCase {

  @Override
  protected Element getTestElement() {
    return new Element(MULE_TAG_NAME).setAttribute(POLICY_NAME_ATTR, POLICY_NAME_ATTR_VALUE);
  }

  private void assertMuleTagName(Element element, int expectedMuleContentSize) {
    assertThat(element.getName(), is(MULE_TAG_NAME));
    assertThat(element.getContent().size(), is(expectedMuleContentSize));
  }

  @Test
  public void convertRawBeforeTag() {
    final CleanupAttributesMigrationStep step = new CleanupAttributesMigrationStep();
    Element element = getTestElement();

    step.execute(element, reportMock);

    assertMuleTagName(element, 0);
  }

  @Test
  public void convertBeforeTagWithContent() {
    final CleanupAttributesMigrationStep step = new CleanupAttributesMigrationStep();
    Element sourceElement = new Element(SOURCE_TAG_NAME, HTTP_POLICY_NAMESPACE);
    sourceElement.addContent(new Element(EXECUTE_NEXT_TAG_NAME, HTTP_POLICY_NAMESPACE));
    sourceElement.addContent(new Element(GENERIC_TAG_NAME).setAttribute(GENERIC_TAG_ATTRIBUTE_NAME, GENERIC_TAG_ATTRIBUTE_VALUE));
    Element element = getTestElement().addContent(new Element(PROXY_TAG_NAME, HTTP_POLICY_NAMESPACE).addContent(sourceElement));

    step.execute(element, reportMock);

    assertMuleTagName(element, 1);
    final Element proxy = (Element) element.getContent(0);
    assertProxyAndSourceElements(proxy, 2);
    assertExecuteNextElement((Element) proxy.getContent(0), 0);
  }
}
