/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.proxy;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.COM_MULESOFT_ANYPOINT_GROUP_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_PLUGIN_CLASSIFIER;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_WSDL_FUNCTIONS_EXTENSION_ARTIFACT_ID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

public class DWWsdlPropertyAttributeValueMigrationStepTestCase {

  private static final String GENERIC_TAG_NAME = "element";
  private static final String HOST_ATTRIBUTE = "host";
  private static final String PORT_ATTRIBUTE = "port";
  private static final String PATH_ATTRIBUTE = "path";
  private static final String NAMESPACE_ATTRIBUTE = "namespace";
  private static final String SERVICE_ATTRIBUTE = "service";
  private static final String GENERIC_ATTRIBUTE = "attribute";

  private static final String PROPERTY_PORT_ATTRIBUTE_MULE3 = "![wsdl(p['wsdl.uri']).services[0].preferredPort.name]";
  private static final String FUNCTION_PORT_ATTRIBUTE_MULE3 =
      "![wsdl(p['wsdl.uri']).services[0].preferredPort.addresses[0].port]";
  private static final String FUNCTION_HOST_ATTRIBUTE_MULE3 =
      "![wsdl(p['wsdl.uri']).services[0].preferredPort.addresses[0].host]";
  private static final String FUNCTION_PATH_ATTRIBUTE_MULE3 =
      "![wsdl(p['wsdl.uri']).services[0].preferredPort.addresses[0].path]";
  private static final String CUSTOM_FUNCTION_PORT_ATTRIBUTE_MULE3 =
      "![wsdl(p['my.wsdl.uri']).services[0].preferredPort.addresses[0].port]";
  private static final String CUSTOM_FUNCTION_HOST_ATTRIBUTE_MULE3 =
      "![wsdl(p['my.wsdl.uri']).services[0].preferredPort.addresses[0].host]";
  private static final String CUSTOM_FUNCTION_PATH_ATTRIBUTE_MULE3 =
      "![wsdl(p['my.wsdl.uri']).services[0].preferredPort.addresses[0].path]";
  private static final String NAMESPACE_ATTRIBUTE_VALUE = "![wsdl(p['wsdl.uri']).targetNamespace]";
  private static final String SERVICE_ATTRIBUTE_VALUE = "![wsdl(p['wsdl.uri']).services[0].name]";
  private static final String GENERIC_ATTRIBUTE_VALUE = "![wsdl(p['wsdl.uri']).services[0].value]";

  private static final String EXPECTED_FUNCTION_PORT = "#[Wsdl::getPort('${wsdl.uri}','${service.name}','${service.port}')]";
  private static final String EXPECTED_FUNCTION_HOST = "#[Wsdl::getHost('${wsdl.uri}','${service.name}','${service.port}')]";
  private static final String EXPECTED_FUNCTION_PATH = "#[Wsdl::getPath('${wsdl.uri}','${service.name}','${service.port}')]";
  private static final String CUSTOM_EXPECTED_FUNCTION_PORT =
      "#[Wsdl::getPort('${my.wsdl.uri}','${service.name}','${service.port}')]";
  private static final String CUSTOM_EXPECTED_FUNCTION_HOST =
      "#[Wsdl::getHost('${my.wsdl.uri}','${service.name}','${service.port}')]";
  private static final String CUSTOM_EXPECTED_FUNCTION_PATH =
      "#[Wsdl::getPath('${my.wsdl.uri}','${service.name}','${service.port}')]";
  private static final String SERVICE_PORT = "service.port";
  private static final String SERVICE_NAMESPACE = "service.namespace";
  private static final String SERVICE_NAME = "service.name";

  private static final String XPATH_NODE_EXPRESSION = "//*[@*[contains(.,'![wsdl(p[')]]";

  private static final Path APPLICATION_MODEL_PATH = Paths.get("src/test/resources/mule/apps/gateway/proxy/original");

  protected MigrationReport reportMock;
  protected ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    reportMock = mock(MigrationReport.class);
    ApplicationModel.ApplicationModelBuilder appModelBuilder = new ApplicationModel.ApplicationModelBuilder();
    appModelBuilder.withProjectType(ProjectType.MULE_THREE_POLICY);
    appModelBuilder.withProjectBasePath(APPLICATION_MODEL_PATH);
    appModelBuilder.withConfigurationFiles(Arrays.asList(APPLICATION_MODEL_PATH.resolve("wsdl-mule3.xml")));
    appModel = appModelBuilder.build();
  }

  private Element getTestElement() {
    return new Element(GENERIC_TAG_NAME);
  }

  @Test
  public void convertSimplePortPropertyAttribute() {
    final DWWsdlPropertyAttributeValueMigrationStep step = new DWWsdlPropertyAttributeValueMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement().setAttribute(PORT_ATTRIBUTE, PROPERTY_PORT_ATTRIBUTE_MULE3);

    step.execute(element, reportMock);

    assertThat(element.getAttributeValue(PORT_ATTRIBUTE), is("${" + SERVICE_PORT + "}"));
    verify(reportMock).report("proxy.wsdlPropertyAttribute", element, element, PROPERTY_PORT_ATTRIBUTE_MULE3,
                              SERVICE_PORT);
  }

  @Test
  public void hardcodedSimplePortPropertyIsNotMigrated() {
    final DWWsdlPropertyAttributeValueMigrationStep step = new DWWsdlPropertyAttributeValueMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement().setAttribute(PORT_ATTRIBUTE, "TestPort12");

    step.execute(element, reportMock);

    assertThat(element.getAttributeValue(PORT_ATTRIBUTE), is("TestPort12"));
  }

  @Test
  public void convertPortInFunctionAttribute() {
    final DWWsdlPropertyAttributeValueMigrationStep step = new DWWsdlPropertyAttributeValueMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement().setAttribute(PORT_ATTRIBUTE, FUNCTION_PORT_ATTRIBUTE_MULE3);

    step.execute(element, reportMock);

    assertThat(element.getAttributeValue(PORT_ATTRIBUTE), is(EXPECTED_FUNCTION_PORT));
    verify(reportMock).report("proxy.functionWsdlPropertyAttribute", element, element, FUNCTION_PORT_ATTRIBUTE_MULE3);
  }

  @Test
  public void convertPropertyAttributes() {
    final DWWsdlPropertyAttributeValueMigrationStep step = new DWWsdlPropertyAttributeValueMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement()
        .setAttribute(PORT_ATTRIBUTE, PROPERTY_PORT_ATTRIBUTE_MULE3)
        .setAttribute(NAMESPACE_ATTRIBUTE, NAMESPACE_ATTRIBUTE_VALUE)
        .setAttribute(SERVICE_ATTRIBUTE, SERVICE_ATTRIBUTE_VALUE);

    step.execute(element, reportMock);

    assertThat(element.getAttributeValue(PORT_ATTRIBUTE), is("${" + SERVICE_PORT + "}"));
    assertThat(element.getAttributeValue(NAMESPACE_ATTRIBUTE), is("${" + SERVICE_NAMESPACE + "}"));
    assertThat(element.getAttributeValue(SERVICE_ATTRIBUTE), is("${" + SERVICE_NAME + "}"));
    verify(reportMock).report("proxy.wsdlPropertyAttribute", element, element, PROPERTY_PORT_ATTRIBUTE_MULE3,
                              SERVICE_PORT);
    verify(reportMock).report("proxy.wsdlPropertyAttribute", element, element, NAMESPACE_ATTRIBUTE_VALUE,
                              SERVICE_NAMESPACE);
    verify(reportMock).report("proxy.wsdlPropertyAttribute", element, element, SERVICE_ATTRIBUTE_VALUE,
                              SERVICE_NAME);
  }

  @Test
  public void convertHardcodedPropertyAttributes() {
    final DWWsdlPropertyAttributeValueMigrationStep step = new DWWsdlPropertyAttributeValueMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement()
        .setAttribute(PORT_ATTRIBUTE, "TestPort12")
        .setAttribute(NAMESPACE_ATTRIBUTE, "http://wsdl.proxy.test.anypoint.mulesoft.com/")
        .setAttribute(SERVICE_ATTRIBUTE, "TestService");

    step.execute(element, reportMock);

    assertThat(element.getAttributeValue(PORT_ATTRIBUTE), is("TestPort12"));
    assertThat(element.getAttributeValue(NAMESPACE_ATTRIBUTE), is("http://wsdl.proxy.test.anypoint.mulesoft.com/"));
    assertThat(element.getAttributeValue(SERVICE_ATTRIBUTE), is("TestService"));
  }

  @Test
  public void convertHardcodedAndParametrizedPropertyAttributes() {
    final DWWsdlPropertyAttributeValueMigrationStep step = new DWWsdlPropertyAttributeValueMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement()
        .setAttribute(PORT_ATTRIBUTE, "TestPort12")
        .setAttribute(NAMESPACE_ATTRIBUTE, NAMESPACE_ATTRIBUTE_VALUE)
        .setAttribute(SERVICE_ATTRIBUTE, "TestService");

    step.execute(element, reportMock);

    assertThat(element.getAttributeValue(PORT_ATTRIBUTE), is("TestPort12"));
    assertThat(element.getAttributeValue(NAMESPACE_ATTRIBUTE), is("${" + SERVICE_NAMESPACE + "}"));
    assertThat(element.getAttributeValue(SERVICE_ATTRIBUTE), is("TestService"));
    verify(reportMock).report("proxy.wsdlPropertyAttribute", element, element, NAMESPACE_ATTRIBUTE_VALUE,
                              SERVICE_NAMESPACE);
  }

  @Test
  public void convertPropertiesInFunctionsAttributes() {
    final DWWsdlPropertyAttributeValueMigrationStep step = new DWWsdlPropertyAttributeValueMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement()
        .setAttribute(PORT_ATTRIBUTE, FUNCTION_PORT_ATTRIBUTE_MULE3)
        .setAttribute(HOST_ATTRIBUTE, FUNCTION_HOST_ATTRIBUTE_MULE3)
        .setAttribute(PATH_ATTRIBUTE, FUNCTION_PATH_ATTRIBUTE_MULE3);

    step.execute(element, reportMock);

    assertThat(element.getAttributeValue(PORT_ATTRIBUTE), is(EXPECTED_FUNCTION_PORT));
    assertThat(element.getAttributeValue(HOST_ATTRIBUTE), is(EXPECTED_FUNCTION_HOST));
    assertThat(element.getAttributeValue(PATH_ATTRIBUTE), is(EXPECTED_FUNCTION_PATH));
    verify(reportMock).report("proxy.functionWsdlPropertyAttribute", element, element, FUNCTION_PORT_ATTRIBUTE_MULE3);
    verify(reportMock).report("proxy.functionWsdlPropertyAttribute", element, element, FUNCTION_HOST_ATTRIBUTE_MULE3);
    verify(reportMock).report("proxy.functionWsdlPropertyAttribute", element, element, FUNCTION_PATH_ATTRIBUTE_MULE3);
  }

  @Test
  public void convertPropertiesInFunctionsWithCustomPropertyName() {
    final DWWsdlPropertyAttributeValueMigrationStep step = new DWWsdlPropertyAttributeValueMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement()
        .setAttribute(PORT_ATTRIBUTE, CUSTOM_FUNCTION_PORT_ATTRIBUTE_MULE3)
        .setAttribute(HOST_ATTRIBUTE, CUSTOM_FUNCTION_HOST_ATTRIBUTE_MULE3)
        .setAttribute(PATH_ATTRIBUTE, CUSTOM_FUNCTION_PATH_ATTRIBUTE_MULE3);

    step.execute(element, reportMock);

    assertThat(element.getAttributeValue(PORT_ATTRIBUTE), is(CUSTOM_EXPECTED_FUNCTION_PORT));
    assertThat(element.getAttributeValue(HOST_ATTRIBUTE), is(CUSTOM_EXPECTED_FUNCTION_HOST));
    assertThat(element.getAttributeValue(PATH_ATTRIBUTE), is(CUSTOM_EXPECTED_FUNCTION_PATH));
    verify(reportMock).report("proxy.functionWsdlPropertyAttribute", element, element, CUSTOM_FUNCTION_PORT_ATTRIBUTE_MULE3);
    verify(reportMock).report("proxy.functionWsdlPropertyAttribute", element, element, CUSTOM_FUNCTION_HOST_ATTRIBUTE_MULE3);
    verify(reportMock).report("proxy.functionWsdlPropertyAttribute", element, element, CUSTOM_FUNCTION_PATH_ATTRIBUTE_MULE3);
  }

  @Test
  public void convertBothAttributes() {
    final DWWsdlPropertyAttributeValueMigrationStep step = new DWWsdlPropertyAttributeValueMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement()
        .setAttribute(PORT_ATTRIBUTE, PROPERTY_PORT_ATTRIBUTE_MULE3)
        .setAttribute(NAMESPACE_ATTRIBUTE, NAMESPACE_ATTRIBUTE_VALUE)
        .setAttribute(SERVICE_ATTRIBUTE, SERVICE_ATTRIBUTE_VALUE)
        .setAttribute(HOST_ATTRIBUTE, FUNCTION_HOST_ATTRIBUTE_MULE3)
        .setAttribute(PATH_ATTRIBUTE, FUNCTION_PATH_ATTRIBUTE_MULE3);

    step.execute(element, reportMock);

    assertThat(element.getAttributeValue(PORT_ATTRIBUTE), is("${" + SERVICE_PORT + "}"));
    assertThat(element.getAttributeValue(NAMESPACE_ATTRIBUTE), is("${" + SERVICE_NAMESPACE + "}"));
    assertThat(element.getAttributeValue(SERVICE_ATTRIBUTE), is("${" + SERVICE_NAME + "}"));
    assertThat(element.getAttributeValue(HOST_ATTRIBUTE), is(EXPECTED_FUNCTION_HOST));
    assertThat(element.getAttributeValue(PATH_ATTRIBUTE), is(EXPECTED_FUNCTION_PATH));
    verify(reportMock).report("proxy.wsdlPropertyAttribute", element, element, PROPERTY_PORT_ATTRIBUTE_MULE3,
                              SERVICE_PORT);
    verify(reportMock).report("proxy.wsdlPropertyAttribute", element, element, NAMESPACE_ATTRIBUTE_VALUE,
                              SERVICE_NAMESPACE);
    verify(reportMock).report("proxy.wsdlPropertyAttribute", element, element, SERVICE_ATTRIBUTE_VALUE,
                              SERVICE_NAME);
    verify(reportMock).report("proxy.functionWsdlPropertyAttribute", element, element, FUNCTION_HOST_ATTRIBUTE_MULE3);
    verify(reportMock).report("proxy.functionWsdlPropertyAttribute", element, element, FUNCTION_PATH_ATTRIBUTE_MULE3);
  }

  @Test
  public void attemptUnknownAttributeConvert() {
    final DWWsdlPropertyAttributeValueMigrationStep step = new DWWsdlPropertyAttributeValueMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement()
        .setAttribute(GENERIC_ATTRIBUTE, GENERIC_ATTRIBUTE_VALUE);

    step.execute(element, reportMock);

    assertThat(element.getAttributeValue(GENERIC_ATTRIBUTE), is(GENERIC_ATTRIBUTE_VALUE));
    verify(reportMock).report("proxy.unknownAttributeValue", element, element, GENERIC_ATTRIBUTE);
  }

  @Test
  public void wsdlPomContributionTest() throws Exception {
    final DWWsdlPropertyAttributeValueMigrationStep step = new DWWsdlPropertyAttributeValueMigrationStep();
    step.setApplicationModel(appModel);
    Element element = appModel.getNode(XPATH_NODE_EXPRESSION);

    step.execute(element, reportMock);

    PomModel pm = appModel.getPomModel().get();

    assertThat(pm.getDependencies().size(), is(1));
    Dependency wsdlExtensionDependency = pm.getDependencies().get(0);
    assertThat(wsdlExtensionDependency.getGroupId(), is(COM_MULESOFT_ANYPOINT_GROUP_ID));
    assertThat(wsdlExtensionDependency.getArtifactId(), is(MULE_WSDL_FUNCTIONS_EXTENSION_ARTIFACT_ID));
    assertThat(wsdlExtensionDependency.getVersion(), is(notNullValue()));
    assertThat(wsdlExtensionDependency.getClassifier(), is(MULE_PLUGIN_CLASSIFIER));

    assertThat(element.getAttributeValue(PORT_ATTRIBUTE), is(EXPECTED_FUNCTION_PORT));
    assertThat(element.getAttributeValue(HOST_ATTRIBUTE), is(EXPECTED_FUNCTION_HOST));
  }

}
