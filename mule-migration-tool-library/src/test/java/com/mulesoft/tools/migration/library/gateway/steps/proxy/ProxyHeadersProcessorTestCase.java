/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.proxy;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.CONFIG;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Before;

public abstract class ProxyHeadersProcessorTestCase {

  protected static final Namespace PROXY_NAMESPACE = Namespace.getNamespace("proxy", "http://www.mulesoft.org/schema/mule/proxy");

  protected static final String CUSTOM_PROCESSOR_TAG_NAME = "custom-processor";
  protected static final String CLASS = "class";

  private static final String NAME = "name";
  protected static final String CONFIG_REF = "config-ref";
  protected static final String PROXY_CONFIG = "proxy-config";
  protected static final String TARGET = "target";

  protected static final String COM_MULESOFT_ANYPOINT_GROUP_ID = "com.mulesoft.anypoint";
  protected static final String MULE_HTTP_PROXY_EXTENSION_ARTIFACT_ID = "mule-http-proxy-extension";
  protected static final String HTTP_PROXY_EXTENSION_VERSION = "1.1.2";
  protected static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";

  protected static final Path APPLICATION_MODEL_PATH = Paths.get("src/test/resources/mule/apps/gateway/proxy/original");

  protected MigrationReport reportMock;
  protected ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    reportMock = mock(MigrationReport.class);
    ApplicationModel.ApplicationModelBuilder appModelBuilder = new ApplicationModel.ApplicationModelBuilder();
    appModelBuilder.withProjectType(ProjectType.MULE_THREE_POLICY);
    appModelBuilder.withProjectBasePath(APPLICATION_MODEL_PATH);
    appModelBuilder.withConfigurationFiles(Arrays.asList(APPLICATION_MODEL_PATH.resolve("custom-processor-mule3.xml")));
    appModel = appModelBuilder.build();
  }

  protected Element getRootElement(final Element element) {
    Document doc = element.getDocument();
    if (doc != null) {
      return doc.getRootElement();
    }
    return null;
  }

  protected void assertConfigElement(Element element) {
    Element root = getRootElement(element);
    assertThat(root.getChildren(CONFIG, PROXY_NAMESPACE).size(), is(1));
    Element config = root.getChild(CONFIG, PROXY_NAMESPACE);
    assertThat(config.getName(), is(CONFIG));
    assertThat(config.getNamespace(), is(PROXY_NAMESPACE));
    assertThat(config.getAttributes().size(), is(1));
    assertThat(config.getAttribute(NAME).getValue(), is(PROXY_CONFIG));
    verify(reportMock).report("proxy.templates", element, config);
  }

  protected abstract Element getTestElement();
}
