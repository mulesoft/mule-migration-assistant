/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.proxy;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Before;
import org.junit.Test;

public class DWPropertyAttributeValueMigrationStepTestCase {

  private static final Namespace GENERIC_NAMESPACE = Namespace.getNamespace("test", "http://test.namespace.org");
  private static final String GENERIC_TAG_NAME = "element";
  private static final String GENERIC_ATTRIBUTE_NAME_1 = "firstAttribute";
  private static final String GENERIC_ATTRIBUTE_VALUE_1 = "![p['first.property']]";
  private static final String GENERIC_ATTRIBUTE_NAME_2 = "secondAttribute";
  private static final String GENERIC_ATTRIBUTE_VALUE_2 = "![p['second.property']]";
  private static final String GENERIC_ATTRIBUTE_NAME_3 = "thirdAttribute";
  private static final String GENERIC_ATTRIBUTE_VALUE_3 = "thirdProperty";
  private static final String WSDL_ATTRIBUTE_VALUE = "![p['wsdl.uri']]";
  private static final String CUSTOM_WSDL_ATTRIBUTE_VALUE = "![p['my.wsdl.uri']]";

  private static final String EXPECTED_GENERIC_ATTRIBUTE_VALUE_1 = "${first.property}";
  private static final String EXPECTED_GENERIC_ATTRIBUTE_VALUE_2 = "${second.property}";
  private static final String EXPECTED_WSDL_ATTRIBUTE_VALUE = "${wsdl.uri}";
  private static final String CUSTOM_EXPECTED_WSDL_ATTRIBUTE_VALUE = "${my.wsdl.uri}";

  protected MigrationReport reportMock;

  @Before
  public void setUp() throws Exception {
    reportMock = mock(MigrationReport.class);
  }

  protected Element getTestElement() {
    return new Element(GENERIC_TAG_NAME).setAttribute(new Attribute(GENERIC_ATTRIBUTE_NAME_1, GENERIC_ATTRIBUTE_VALUE_1));
  }

  @Test
  public void convertSimpleElement() {
    final DWPropertyAttributeValueMigrationStep step = new DWPropertyAttributeValueMigrationStep();
    Element element = getTestElement();

    step.execute(element, reportMock);

    assertThat(element.getAttribute(GENERIC_ATTRIBUTE_NAME_1).getValue(), is(EXPECTED_GENERIC_ATTRIBUTE_VALUE_1));
  }

  @Test
  public void convertElementWithTwoAttributes() {
    final DWPropertyAttributeValueMigrationStep step = new DWPropertyAttributeValueMigrationStep();
    Element element = getTestElement().setAttribute(GENERIC_ATTRIBUTE_NAME_2, GENERIC_ATTRIBUTE_VALUE_2);

    step.execute(element, reportMock);

    assertThat(element.getAttribute(GENERIC_ATTRIBUTE_NAME_1).getValue(), is(EXPECTED_GENERIC_ATTRIBUTE_VALUE_1));
    assertThat(element.getAttribute(GENERIC_ATTRIBUTE_NAME_2).getValue(), is(EXPECTED_GENERIC_ATTRIBUTE_VALUE_2));
  }

  @Test
  public void convertOneAttributeOutOfTwo() {
    final DWPropertyAttributeValueMigrationStep step = new DWPropertyAttributeValueMigrationStep();
    Element element = getTestElement().setAttribute(GENERIC_ATTRIBUTE_NAME_3, GENERIC_ATTRIBUTE_VALUE_3);

    step.execute(element, reportMock);

    assertThat(element.getAttribute(GENERIC_ATTRIBUTE_NAME_1).getValue(), is(EXPECTED_GENERIC_ATTRIBUTE_VALUE_1));
    assertThat(element.getAttribute(GENERIC_ATTRIBUTE_NAME_3).getValue(), is(GENERIC_ATTRIBUTE_VALUE_3));
  }

  @Test
  public void convertSimpleElementWithNamespace() {
    final DWPropertyAttributeValueMigrationStep step = new DWPropertyAttributeValueMigrationStep();
    Element element = getTestElement().setNamespace(GENERIC_NAMESPACE);

    step.execute(element, reportMock);

    assertThat(element.getAttribute(GENERIC_ATTRIBUTE_NAME_1).getValue(), is(EXPECTED_GENERIC_ATTRIBUTE_VALUE_1));
  }

  @Test
  public void convertSimpleElementWithWSDLUriAttribute() {
    final DWPropertyAttributeValueMigrationStep step = new DWPropertyAttributeValueMigrationStep();
    Element element = getTestElement().setAttribute(GENERIC_ATTRIBUTE_NAME_2, WSDL_ATTRIBUTE_VALUE);

    step.execute(element, reportMock);

    assertThat(element.getAttribute(GENERIC_ATTRIBUTE_NAME_1).getValue(), is(EXPECTED_GENERIC_ATTRIBUTE_VALUE_1));
    assertThat(element.getAttribute(GENERIC_ATTRIBUTE_NAME_2).getValue(), is(EXPECTED_WSDL_ATTRIBUTE_VALUE));
  }

  @Test
  public void convertSimpleElementWithCustomWSDLUriAttribute() {
    final DWPropertyAttributeValueMigrationStep step = new DWPropertyAttributeValueMigrationStep();
    Element element = getTestElement().setAttribute(GENERIC_ATTRIBUTE_NAME_2, CUSTOM_WSDL_ATTRIBUTE_VALUE);

    step.execute(element, reportMock);

    assertThat(element.getAttribute(GENERIC_ATTRIBUTE_NAME_1).getValue(), is(EXPECTED_GENERIC_ATTRIBUTE_VALUE_1));
    assertThat(element.getAttribute(GENERIC_ATTRIBUTE_NAME_2).getValue(), is(CUSTOM_EXPECTED_WSDL_ATTRIBUTE_VALUE));
  }

}
