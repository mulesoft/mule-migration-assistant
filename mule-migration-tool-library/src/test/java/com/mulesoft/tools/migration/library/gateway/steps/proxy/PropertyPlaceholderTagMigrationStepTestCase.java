/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.proxy;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_4_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_4_NAMESPACE_URI;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Before;
import org.junit.Test;

public class PropertyPlaceholderTagMigrationStepTestCase {

  private static final String CONFIGURATION_PROPERTIES_TAG_NAME = "configuration-properties";
  private static final String PROPERTY_PLACEHOLDER_TAG_NAME = "property-placeholder";
  private static final String MULE_3_NAMESPACE_PREFIX = "expression-language";
  private static final String MULE_3_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/expression-language-gw";
  private static final Namespace MULE_3_NAMESPACE =
      Namespace.getNamespace(MULE_3_NAMESPACE_PREFIX, MULE_3_NAMESPACE_URI);

  private static final String LOCATION = "location";
  private static final String FILE = "file";

  private static final String CONFIG_PROPERTIES = "config.properties";

  protected MigrationReport reportMock;

  @Before
  public void setUp() throws Exception {
    reportMock = mock(MigrationReport.class);
  }

  protected Element getTestElement() {
    return new Element(PROPERTY_PLACEHOLDER_TAG_NAME, MULE_3_NAMESPACE).setAttribute(new Attribute(LOCATION, CONFIG_PROPERTIES));
  }

  @Test
  public void convertRawApiTag() {
    final PropertyPlaceholderTagMigrationStep step = new PropertyPlaceholderTagMigrationStep();
    Element element = getTestElement();

    step.execute(element, reportMock);

    assertThat(element.getName(), is(CONFIGURATION_PROPERTIES_TAG_NAME));
    assertThat(element.getNamespace(), is(MULE_4_NAMESPACE));
    assertThat(element.getNamespaceURI(), is(MULE_4_NAMESPACE_URI));
    assertThat(element.getAttributes().size(), is(1));
    assertThat(element.getAttribute(FILE).getValue(), is(CONFIG_PROPERTIES));
    assertThat(element.getContent().size(), is(0));
  }

}
