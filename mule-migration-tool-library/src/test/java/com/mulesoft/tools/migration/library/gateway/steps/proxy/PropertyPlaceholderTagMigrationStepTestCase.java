/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.proxy;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_4_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_4_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_TAG_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.Arrays;

import org.jdom2.Attribute;
import org.jdom2.Document;
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
    Element root = new Element(MULE_TAG_NAME, MULE_4_NAMESPACE);
    new Document().setRootElement(root);
    Element element =
        new Element(PROPERTY_PLACEHOLDER_TAG_NAME, MULE_3_NAMESPACE).setAttribute(new Attribute(LOCATION, CONFIG_PROPERTIES));
    root.addContent(Arrays.asList(element));
    return element;
  }

  @Test
  public void convertRawApiTag() {
    final PropertyPlaceholderTagMigrationStep step = new PropertyPlaceholderTagMigrationStep();
    Element element = getTestElement();
    Element root = element.getDocument().getRootElement();

    step.execute(element, reportMock);

    assertThat(root.getChildren().size(), is(1));
    Element object = root.getChildren().get(0);
    assertThat(object.getName(), is(CONFIGURATION_PROPERTIES_TAG_NAME));
    assertThat(object.getNamespace(), is(MULE_4_NAMESPACE));
    assertThat(object.getNamespaceURI(), is(MULE_4_NAMESPACE_URI));
    assertThat(object.getAttributes().size(), is(1));
    assertThat(object.getAttribute(FILE).getValue(), is(CONFIG_PROPERTIES));
    assertThat(object.getContent().size(), is(0));
  }

}
