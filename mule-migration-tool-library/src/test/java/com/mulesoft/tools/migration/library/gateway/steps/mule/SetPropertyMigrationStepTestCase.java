/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.mule;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.COM_MULESOFT_ANYPOINT_GROUP_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.HTTP_POLICY_TRANSFORM_EXTENSION_VERSION;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MIGRATION_RESOURCES_PATH;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_HTTP_POLICY_TRANSFORM_EXTENSION_ARTIFACT_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_PLUGIN_CLASSIFIER;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.POLICY_APPLICATION_MODEL_PATH;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.POLICY_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.SET_PROPERTY_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.SET_RESPONSE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.COMPATIBILITY_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_TRANSFORM_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_3_POLICY_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_POLICY_NAMESPACE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.library.gateway.steps.policy.mule.SetPropertyMigrationStep;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class SetPropertyMigrationStepTestCase {

  private static final String GENERIC_ATTR_NAME = "attr";
  private static final String GENERIC_ATTR_VALUE = "value";
  private static final String GENERIC_TAG_NAME = "tag";

  private static final String OUTBOUND_PROPERTIES_TO_VAR_TAG_NAME = "outbound-properties-to-var";
  private static final String STATUS_CODE_ATTR_NAME = "statusCode";
  private static final String HTTP_LISTENER_RESPONSE_SUCCESS_STATUS_CODE_DWL =
      "#[migration::HttpListener::httpListenerResponseSuccessStatusCode(vars)]";
  private static final String HEADERS_TAG_NAME = "headers";
  private static final String HTTP_LISTENER_RESPONSE_HEADERS_DWL =
      "#[migration::HttpListener::httpListenerResponseHeaders(vars)]";

  private static final String XPATH_NODE_EXPRESSION =
      "//*[namespace-uri() = 'http://www.mulesoft.org/schema/mule/compatibility' and local-name() = 'set-property']";
  private static final Path APPLICATION_MODEL_PATH = Paths.get("src/test/resources/mule/apps/gateway/mule/original");

  private MigrationReport reportMock;
  private ApplicationModel appModel;

  private Element getTestElement() {
    return new Element(SET_PROPERTY_TAG_NAME, COMPATIBILITY_NAMESPACE).setAttribute(GENERIC_ATTR_NAME, GENERIC_ATTR_VALUE);
  }

  private Element setDocument(Element... setPropertyElements) {
    Element root = new Element(POLICY_TAG_NAME, MULE_3_POLICY_NAMESPACE);
    new Document().setRootElement(root);
    root.addContent(Arrays.asList(setPropertyElements));
    return root;
  }

  private Element getSetResponseElement() {
    return new Element(SET_RESPONSE_TAG_NAME, HTTP_TRANSFORM_NAMESPACE)
        .setAttribute(STATUS_CODE_ATTR_NAME, HTTP_LISTENER_RESPONSE_SUCCESS_STATUS_CODE_DWL)
        .addContent(new Element(HEADERS_TAG_NAME, HTTP_TRANSFORM_NAMESPACE).addContent(HTTP_LISTENER_RESPONSE_HEADERS_DWL));
  }

  private Element getOutboundPropertiesToVarElement() {
    return new Element(OUTBOUND_PROPERTIES_TO_VAR_TAG_NAME, COMPATIBILITY_NAMESPACE);
  }

  private void assertTestElementIsNotModified(Element element) {
    assertThat(element.getName(), is(SET_PROPERTY_TAG_NAME));
    assertThat(element.getAttributeValue(GENERIC_ATTR_NAME), is(GENERIC_ATTR_VALUE));
  }

  private void assertOutboundPropertiesToVarElement(Element outboundPropertiesToVarElement) {
    assertThat(outboundPropertiesToVarElement.getName(), is(OUTBOUND_PROPERTIES_TO_VAR_TAG_NAME));
    assertThat(outboundPropertiesToVarElement.getNamespace(), is(COMPATIBILITY_NAMESPACE));
  }

  private void assertSetResponseElement(Element setResponseElement) {
    assertThat(setResponseElement.getName(), is(SET_RESPONSE_TAG_NAME));
    assertThat(setResponseElement.getNamespace(), is(HTTP_TRANSFORM_NAMESPACE));
    assertThat(setResponseElement.getAttributeValue(STATUS_CODE_ATTR_NAME), is(HTTP_LISTENER_RESPONSE_SUCCESS_STATUS_CODE_DWL));
    assertThat(setResponseElement.getContentSize(), is(1));
    Element headersElement = setResponseElement.getChild(HEADERS_TAG_NAME, HTTP_TRANSFORM_NAMESPACE);
    assertThat(headersElement.getContentSize(), is(1));
    assertThat(headersElement.getContent(0).getValue(), is(HTTP_LISTENER_RESPONSE_HEADERS_DWL));
  }

  private void assertInnerMigratedElement(Element genericElement) {
    assertThat(genericElement.getContentSize(), is(3));
    assertOutboundPropertiesToVarElement((Element) genericElement.getContent(1));
    assertSetResponseElement((Element) genericElement.getContent(2));
  }

  private void assertGenericTagElement(Element genericTagElement, int expectedGenericContentSize) {
    assertThat(genericTagElement.getName(), is(GENERIC_TAG_NAME));
    assertThat(genericTagElement.getContentSize(), is(expectedGenericContentSize));
    assertThat(((Element) genericTagElement.getContent(0)).getName(), is(SET_PROPERTY_TAG_NAME));
    if (expectedGenericContentSize > 1) {
      assertInnerMigratedElement(genericTagElement);
    }
  }

  private void performNestedElementTestsAssertions(Element rootElement, int expectedRootElementContentSize,
                                                   int expectedOutboundPropertiesToVarElementPosition,
                                                   int expectedSetResponseElementPosition, int expectedGenericElementPosition,
                                                   int expectedGenericElementContentSize) {
    assertThat(rootElement.getContentSize(), is(expectedRootElementContentSize));
    assertTestElementIsNotModified((Element) rootElement.getContent(0));
    assertOutboundPropertiesToVarElement((Element) rootElement.getContent(expectedOutboundPropertiesToVarElementPosition));
    assertSetResponseElement((Element) rootElement.getContent(expectedSetResponseElementPosition));
    Element genericTagElement = (Element) rootElement.getContent(expectedGenericElementPosition);
    assertGenericTagElement(genericTagElement, expectedGenericElementContentSize);
  }

  @Before
  public void setUp() throws Exception {
    reportMock = mock(MigrationReport.class);
    ApplicationModel.ApplicationModelBuilder amb = new ApplicationModel.ApplicationModelBuilder();
    amb.withProjectType(ProjectType.MULE_THREE_POLICY);
    amb.withProjectBasePath(POLICY_APPLICATION_MODEL_PATH);
    amb.withConfigurationFiles(Arrays.asList(APPLICATION_MODEL_PATH.resolve("simple-set-property-mule3.xml")));
    appModel = amb.build();
  }

  @Test
  public void singleSetPropertyElement() {
    SetPropertyMigrationStep step = new SetPropertyMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement();
    Element rootElement = setDocument(element);

    step.execute(element, reportMock);

    assertTestElementIsNotModified(element);
    assertThat(rootElement.getContentSize(), is(3));
    assertThat(((Element) rootElement.getContent(0)).getName(), is(SET_PROPERTY_TAG_NAME));
    assertOutboundPropertiesToVarElement((Element) rootElement.getContent(1));
    assertSetResponseElement((Element) rootElement.getContent(2));
  }

  @Test
  public void doubleSetPropertyElement() {
    SetPropertyMigrationStep step = new SetPropertyMigrationStep();
    step.setApplicationModel(appModel);
    Element firstSetPropertyElement = getTestElement();
    Element secondSetPropertyElement = getTestElement();
    Element rootElement = setDocument(firstSetPropertyElement, secondSetPropertyElement);

    step.execute(firstSetPropertyElement, reportMock);
    step.execute(secondSetPropertyElement, reportMock);

    assertTestElementIsNotModified(firstSetPropertyElement);
    assertTestElementIsNotModified(secondSetPropertyElement);
    assertThat(rootElement.getContentSize(), is(4));
    assertThat(((Element) rootElement.getContent(0)).getName(), is(SET_PROPERTY_TAG_NAME));
    assertThat(((Element) rootElement.getContent(1)).getName(), is(SET_PROPERTY_TAG_NAME));
    assertOutboundPropertiesToVarElement((Element) rootElement.getContent(2));
    assertSetResponseElement((Element) rootElement.getContent(3));
  }

  @Test
  public void singleSetPropertyElementWithSetResponse() {
    SetPropertyMigrationStep step = new SetPropertyMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement();
    Element rootElement = setDocument(element, getSetResponseElement());

    step.execute(element, reportMock);

    assertTestElementIsNotModified(element);
    assertThat(rootElement.getContentSize(), is(3));
    assertThat(((Element) rootElement.getContent(0)).getName(), is(SET_PROPERTY_TAG_NAME));
    assertOutboundPropertiesToVarElement((Element) rootElement.getContent(1));
    assertSetResponseElement((Element) rootElement.getContent(2));
  }

  @Test
  public void doubleSetPropertyElementWithSetResponse() {
    SetPropertyMigrationStep step = new SetPropertyMigrationStep();
    step.setApplicationModel(appModel);
    Element firstSetPropertyElement = getTestElement();
    Element secondSetPropertyElement = getTestElement();
    Element rootElement = setDocument(firstSetPropertyElement, secondSetPropertyElement, getSetResponseElement());

    step.execute(firstSetPropertyElement, reportMock);
    step.execute(secondSetPropertyElement, reportMock);

    assertTestElementIsNotModified(firstSetPropertyElement);
    assertTestElementIsNotModified(secondSetPropertyElement);
    assertThat(rootElement.getContentSize(), is(4));
    assertThat(((Element) rootElement.getContent(0)).getName(), is(SET_PROPERTY_TAG_NAME));
    assertThat(((Element) rootElement.getContent(1)).getName(), is(SET_PROPERTY_TAG_NAME));
    assertOutboundPropertiesToVarElement((Element) rootElement.getContent(2));
    assertSetResponseElement((Element) rootElement.getContent(3));
  }

  @Test
  public void nestedSetPropertyElement() {
    SetPropertyMigrationStep step = new SetPropertyMigrationStep();
    step.setApplicationModel(appModel);
    Element firstSetPropertyElement = getTestElement();
    Element secondSetPropertyElement = getTestElement();
    Element genericElement = new Element(GENERIC_TAG_NAME, MULE_4_POLICY_NAMESPACE).addContent(secondSetPropertyElement);
    Element rootElement = setDocument(firstSetPropertyElement, genericElement);

    step.execute(firstSetPropertyElement, reportMock);

    performNestedElementTestsAssertions(rootElement, 4, 2, 3, 1, 1);

    step.execute(secondSetPropertyElement, reportMock);

    performNestedElementTestsAssertions(rootElement, 4, 2, 3, 1, 3);
  }

  @Test
  public void nestedSetPropertyElementInnerMigrated() {
    SetPropertyMigrationStep step = new SetPropertyMigrationStep();
    step.setApplicationModel(appModel);
    Element firstSetPropertyElement = getTestElement();
    Element genericElement = new Element(GENERIC_TAG_NAME, MULE_4_POLICY_NAMESPACE);
    Element secondSetPropertyElement = getTestElement();
    genericElement
        .addContent(Arrays.asList(secondSetPropertyElement, getOutboundPropertiesToVarElement(), getSetResponseElement()));
    Element rootElement = setDocument(firstSetPropertyElement, genericElement);

    step.execute(firstSetPropertyElement, reportMock);

    performNestedElementTestsAssertions(rootElement, 4, 2, 3, 1, 3);

    step.execute(secondSetPropertyElement, reportMock);

    performNestedElementTestsAssertions(rootElement, 4, 2, 3, 1, 3);
  }

  @Test
  public void nestedSetPropertyElementOuterMigrated() {
    SetPropertyMigrationStep step = new SetPropertyMigrationStep();
    step.setApplicationModel(appModel);
    Element firstSetPropertyElement = getTestElement();
    Element secondSetPropertyElement = getTestElement();
    Element genericElement = new Element(GENERIC_TAG_NAME, MULE_4_POLICY_NAMESPACE).addContent(secondSetPropertyElement);
    Element rootElement =
        setDocument(firstSetPropertyElement, getOutboundPropertiesToVarElement(), getSetResponseElement(), genericElement);

    step.execute(firstSetPropertyElement, reportMock);

    performNestedElementTestsAssertions(rootElement, 4, 1, 2, 3, 1);

    step.execute(secondSetPropertyElement, reportMock);

    performNestedElementTestsAssertions(rootElement, 4, 1, 2, 3, 3);
  }

  @Test
  public void pomContributionsInSetPropertyTest() {
    SetPropertyMigrationStep step = new SetPropertyMigrationStep();
    step.setApplicationModel(appModel);
    Element element = appModel.getNodes(XPATH_NODE_EXPRESSION).get(0);

    step.execute(element, reportMock);

    PomModel pm = appModel.getPomModel().get();

    assertThat(pm.getDependencies().size(), is(1));
    Dependency httpPolicyTransformExtension = pm.getDependencies().get(0);
    assertThat(httpPolicyTransformExtension.getGroupId(), is(COM_MULESOFT_ANYPOINT_GROUP_ID));
    assertThat(httpPolicyTransformExtension.getArtifactId(), is(MULE_HTTP_POLICY_TRANSFORM_EXTENSION_ARTIFACT_ID));
    assertThat(httpPolicyTransformExtension.getVersion(), is(HTTP_POLICY_TRANSFORM_EXTENSION_VERSION));
    assertThat(httpPolicyTransformExtension.getClassifier(), is(MULE_PLUGIN_CLASSIFIER));

  }

  @AfterClass
  public static void cleanup() throws Exception {
    File dwFile = POLICY_APPLICATION_MODEL_PATH.resolve(MIGRATION_RESOURCES_PATH).resolve("HttpListener.dwl").toFile();
    if (!dwFile.delete()) {
      dwFile.deleteOnExit();
    }
  }
}
