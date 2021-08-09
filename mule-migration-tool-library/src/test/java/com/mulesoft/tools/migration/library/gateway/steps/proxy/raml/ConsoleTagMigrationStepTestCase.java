/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.proxy.raml;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.APIKIT_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.CONFIG_REF;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.FLOW_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_4_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.REST_VALIDATOR_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.VALIDATE_REQUEST_TAG_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Before;
import org.junit.Test;


public class ConsoleTagMigrationStepTestCase {

  private static final String CONSOLE_TAG_NAME = "console";
  private static final String CONFIG_REF_ATTR_VALUE = "proxy-config";

  private MigrationReport reportMock;

  @Before
  public void setUp() throws Exception {
    reportMock = mock(MigrationReport.class);
  }

  private Element getTestElement() {
    Element consoleElement = new Element(CONSOLE_TAG_NAME, APIKIT_NAMESPACE).setAttribute(CONFIG_REF, CONFIG_REF_ATTR_VALUE);
    Element muleElement = new Element(MULE_TAG_NAME, MULE_4_NAMESPACE)
        .addContent(new Element(FLOW_TAG_NAME, MULE_4_NAMESPACE)
            .addContent(consoleElement));
    new Document().setRootElement(muleElement);
    return consoleElement;
  }

  private void setRestValidatorElement(Element rootElement) {
    rootElement.addContent(new Element(FLOW_TAG_NAME, MULE_4_NAMESPACE)
        .addContent(new Element(VALIDATE_REQUEST_TAG_NAME, REST_VALIDATOR_NAMESPACE)
            .setAttribute(CONFIG_REF, CONFIG_REF_ATTR_VALUE)));
  }

  @Test
  public void migrateConsoleTag() {
    ConsoleTagMigrationStep step = new ConsoleTagMigrationStep();
    Element consoleElement = getTestElement();
    setRestValidatorElement(consoleElement.getDocument().getRootElement());

    step.execute(consoleElement, reportMock);

    assertConsoleElement(consoleElement, REST_VALIDATOR_NAMESPACE);
  }

  @Test
  public void consoleWithNoRestValidatorIsNotMigrated() {
    ConsoleTagMigrationStep step = new ConsoleTagMigrationStep();
    Element consoleElement = getTestElement();

    step.execute(consoleElement, reportMock);

    assertConsoleElement(consoleElement, APIKIT_NAMESPACE);
  }

  private void assertConsoleElement(Element consoleElement, Namespace targetNamespace) {
    assertThat(consoleElement.getName(), is(CONSOLE_TAG_NAME));
    assertThat(consoleElement.getNamespace(), is(targetNamespace));
    assertThat(consoleElement.getAttributes().size(), is(1));
    assertThat(consoleElement.getAttributeValue(CONFIG_REF), is(CONFIG_REF_ATTR_VALUE));
  }

}
