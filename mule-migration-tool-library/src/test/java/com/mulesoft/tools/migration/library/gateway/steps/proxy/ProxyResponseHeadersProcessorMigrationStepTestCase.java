/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.proxy;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_4_NAMESPACE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.junit.Test;

public class ProxyResponseHeadersProcessorMigrationStepTestCase extends ProxyHeadersProcessorTestCase {

  private static final String PROXY_RESPONSE_HEADERS_PROCESSOR = "com.mulesoft.gateway.extension.ProxyResponseHeadersProcessor";

  private static final String RESPONSE_HEADERS = "response-headers";
  private static final String PROXY_RESPONSE_HEADERS = "proxyResponseHeaders";

  private static final String XPATH_NODE_EXPRESSION =
      "//*[namespace-uri() = 'http://www.mulesoft.org/schema/mule/core' and local-name() = 'custom-processor'][@*[local-name()='class' and .='com.mulesoft.gateway.extension.ProxyResponseHeadersProcessor']]";

  @Override
  protected Element getTestElement() {
    return new Element(CUSTOM_PROCESSOR_TAG_NAME, MULE_4_NAMESPACE)
        .setAttribute(new Attribute(CLASS, PROXY_RESPONSE_HEADERS_PROCESSOR));
  }

  @Test
  public void convertProxyResponseHeadersProcessor() throws Exception {
    final ProxyResponseHeadersProcessorMigrationStep step = new ProxyResponseHeadersProcessorMigrationStep();
    Element element = getTestElement();

    step.setApplicationModel(appModel);

    step.execute(element, reportMock);

    assertThat(element.getName(), is(RESPONSE_HEADERS));
    assertThat(element.getNamespace(), is(PROXY_NAMESPACE));
    assertThat(element.getAttributes().size(), is(2));
    assertThat(element.getAttribute(CONFIG_REF).getValue(), is(PROXY_CONFIG));
    assertThat(element.getAttribute(TARGET).getValue(), is(PROXY_RESPONSE_HEADERS));
    assertThat(element.getContent().size(), is(0));
    assertConfigElement(element);
  }

  @Test
  public void customProcessorPomContributionTest() throws Exception {
    final ProxyResponseHeadersProcessorMigrationStep step = new ProxyResponseHeadersProcessorMigrationStep();

    step.setApplicationModel(appModel);

    step.execute(appModel.getNode(XPATH_NODE_EXPRESSION), reportMock);

    PomModel pm = appModel.getPomModel().get();

    assertThat(pm.getDependencies().size(), is(1));
    Dependency customProcessorDependency = pm.getDependencies().get(0);
    assertThat(customProcessorDependency.getGroupId(), is(COM_MULESOFT_ANYPOINT_GROUP_ID));
    assertThat(customProcessorDependency.getArtifactId(), is(MULE_HTTP_PROXY_EXTENSION_ARTIFACT_ID));
    assertThat(customProcessorDependency.getVersion(), is(notNullValue()));
    assertThat(customProcessorDependency.getClassifier(), is(MULE_PLUGIN_CLASSIFIER));
  }

  @Test
  public void migrateElementAndPomTest() throws Exception {
    final ProxyResponseHeadersProcessorMigrationStep step = new ProxyResponseHeadersProcessorMigrationStep();
    Element element = appModel.getNode(XPATH_NODE_EXPRESSION);

    step.setApplicationModel(appModel);

    step.execute(element, reportMock);

    assertThat(element.getName(), is(RESPONSE_HEADERS));
    assertThat(element.getNamespace(), is(PROXY_NAMESPACE));
    assertThat(element.getAttributes().size(), is(2));
    assertThat(element.getAttribute(CONFIG_REF).getValue(), is(PROXY_CONFIG));
    assertThat(element.getAttribute(TARGET).getValue(), is(PROXY_RESPONSE_HEADERS));
    assertThat(element.getContent().size(), is(0));
    assertConfigElement(element);

    PomModel pm = appModel.getPomModel().get();

    assertThat(pm.getDependencies().size(), is(1));
    Dependency customProcessorDependency = pm.getDependencies().get(0);
    assertThat(customProcessorDependency.getGroupId(), is(COM_MULESOFT_ANYPOINT_GROUP_ID));
    assertThat(customProcessorDependency.getArtifactId(), is(MULE_HTTP_PROXY_EXTENSION_ARTIFACT_ID));
    assertThat(customProcessorDependency.getVersion(), is(notNullValue()));
    assertThat(customProcessorDependency.getClassifier(), is(MULE_PLUGIN_CLASSIFIER));
  }

}
