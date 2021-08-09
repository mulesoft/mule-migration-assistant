/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.proxy;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_ATTRIBUTE_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_ATTRIBUTE_VALUE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_VALUE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Before;
import org.junit.Test;

public class ApiTagMigrationStepTestCase {

  private static final String AUTODISCOVERY_TAG_NAME = "autodiscovery";
  private static final String API_GW_MULE_4_NAMESPACE_PREFIX = "api-gateway";
  private static final String API_GW_MULE_4_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/api-gateway";
  private static final Namespace API_GW_MULE_4_NAMESPACE =
      Namespace.getNamespace(API_GW_MULE_4_NAMESPACE_PREFIX, API_GW_MULE_4_NAMESPACE_URI);
  private static final String API_TAG_NAME = "api";
  private static final String API_GW_MULE_3_NAMESPACE_PREFIX = "api-platform-gw";
  private static final String API_GW_MULE_3_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/api-platform-gw";
  private static final Namespace API_GW_MULE_3_NAMESPACE =
      Namespace.getNamespace(API_GW_MULE_3_NAMESPACE_PREFIX, API_GW_MULE_3_NAMESPACE_URI);

  private static final String FLOW_REF = "flowRef";
  private static final String PROXY = "proxy";

  private static final String API_ID = "apiId";
  private static final String API_ID_VALUE = "${api.id}";

  private static final String API_NAME = "apiName";
  private static final String API_NAME_VALUE = "![p['api.name']]";

  protected MigrationReport reportMock;

  @Before
  public void setUp() throws Exception {
    reportMock = mock(MigrationReport.class);
  }

  protected Element getTestElement() {
    return new Element(API_TAG_NAME, API_GW_MULE_3_NAMESPACE).setAttribute(new Attribute(FLOW_REF, PROXY))
        .setAttribute(new Attribute(API_NAME, API_NAME_VALUE));
  }

  private void assertAutodiscoveryElement(Element element) {
    assertThat(element.getName(), is(AUTODISCOVERY_TAG_NAME));
    assertThat(element.getNamespace(), is(API_GW_MULE_4_NAMESPACE));
    assertThat(element.getNamespacePrefix(), is(API_GW_MULE_4_NAMESPACE_PREFIX));
    assertThat(element.getNamespaceURI(), is(API_GW_MULE_4_NAMESPACE_URI));
    assertThat(element.getAttributes().size(), is(2));
    assertThat(element.getAttribute(FLOW_REF).getValue(), is(PROXY));
    assertThat(element.getAttribute(API_ID).getValue(), is(API_ID_VALUE));
    assertThat(element.getContent().size(), is(0));
  }

  @Test
  public void convertRawApiTag() {
    final ApiTagMigrationStep step = new ApiTagMigrationStep();
    Element element = getTestElement();

    step.execute(element, reportMock);

    assertAutodiscoveryElement(element);
  }

  @Test
  public void convertApiTagWithContent() {
    final ApiTagMigrationStep step = new ApiTagMigrationStep();
    Element element = getTestElement().addContent(new Element(GENERIC_TAG_NAME)
        .setText(GENERIC_TAG_VALUE)
        .setAttribute(GENERIC_TAG_ATTRIBUTE_NAME, GENERIC_TAG_ATTRIBUTE_VALUE));

    step.execute(element, reportMock);

    assertAutodiscoveryElement(element);
  }
}
