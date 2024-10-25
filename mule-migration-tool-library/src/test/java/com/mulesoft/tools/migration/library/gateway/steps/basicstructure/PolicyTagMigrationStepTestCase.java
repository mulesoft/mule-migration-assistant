/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.basicstructure;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_ATTRIBUTE_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_ATTRIBUTE_VALUE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_VALUE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_4_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_4_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.POLICY_ID_ATTR_VALUE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.POLICY_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.SCHEMA_LOCATION;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.TRUE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.XSI_NAMESPACE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.mulesoft.tools.migration.library.gateway.steps.policy.basicstructure.PolicyTagMigrationStep;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.junit.Test;

public class PolicyTagMigrationStepTestCase extends BasicPolicyStructureMigrationTestCase {

  private static final String MULE_4_XSI_SCHEMA_LOCATION_URI =
      "http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd";
  private static final String MULE_3_POLICY_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/policy";
  private static final String ONLINE = "online";

  @Override
  protected Element getTestElement() {
    return new Element(POLICY_TAG_NAME, MULE_3_POLICY_NAMESPACE_URI)
        .setAttribute(ONLINE, TRUE)
        .setAttribute(POLICY_NAME_ATTR, POLICY_NAME_ATTR_VALUE)
        .setAttribute(ID, POLICY_ID_ATTR_VALUE);
  }

  private void assertMuleTagName(Element element, boolean hasContent) {
    assertThat(element.getName(), is(MULE_TAG_NAME));
    assertThat(element.getNamespace(), is(MULE_4_NAMESPACE));
    assertThat(element.getAttributes().size(), is(2));
    assertThat(element.getAttribute(POLICY_NAME_ATTR).getValue(), is(POLICY_NAME_ATTR_VALUE));
    assertThat(element.getAttribute(SCHEMA_LOCATION, XSI_NAMESPACE).getValue(),
               is(new Attribute(SCHEMA_LOCATION, MULE_4_XSI_SCHEMA_LOCATION_URI, XSI_NAMESPACE).getValue()));
    assertThat(element.getContentSize(), is(hasContent ? 1 : 0));
  }

  @Test
  public void convertRawPolicy() {
    final PolicyTagMigrationStep step = new PolicyTagMigrationStep();
    Element element = getTestElement();

    step.execute(element, reportMock);

    assertMuleTagName(element, false);
  }

  @Test
  public void convertPolicyKeepingContent() {
    final PolicyTagMigrationStep step = new PolicyTagMigrationStep();
    Element element = getTestElement()
        .addContent(new Element(GENERIC_TAG_NAME)
            .setText(GENERIC_TAG_VALUE)
            .setAttribute(GENERIC_TAG_ATTRIBUTE_NAME, GENERIC_TAG_ATTRIBUTE_VALUE)
            .setNamespace(GENERIC_NAMESPACE));

    step.execute(element, reportMock);

    assertMuleTagName(element, true);
    assertThat(element.getAttribute(SCHEMA_LOCATION, XSI_NAMESPACE).getValue(),
               is(new Attribute(SCHEMA_LOCATION, MULE_4_XSI_SCHEMA_LOCATION_URI, XSI_NAMESPACE).getValue()));
    final Element genericElement = (Element) element.getContent().get(0);
    assertThat(genericElement.getParentElement().getName(), is(MULE_TAG_NAME));
    assertThat(genericElement.getParentElement().getNamespaceURI(), is(MULE_4_NAMESPACE_URI));
    assertThat(genericElement.getValue(), is(GENERIC_TAG_VALUE));
    assertThat(genericElement.getCType().name(), is(Element.class.getSimpleName()));
    assertThat(genericElement.getNamespacesIntroduced().size(), is(2));
    assertThat(getElementNamespaces(element), is(GENERIC_NAMESPACE_URI));
  }

}
