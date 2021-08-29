/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.basicstructure;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.EXECUTE_NEXT_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.PROXY_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.SOURCE_TAG_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Before;

public abstract class BasicPolicyStructureMigrationTestCase {

  protected static final Path APPLICATION_MODEL_PATH =
      Paths.get("src/test/resources/mule/apps/gateway/basic-policy-structure/expected");

  protected static final String GENERIC_NAMESPACE_PREFIX = "gnp";
  protected static final String GENERIC_NAMESPACE_URI = "http://wwww.testnamespace.com/generic/namespace/uri";
  protected static final Namespace GENERIC_NAMESPACE = Namespace.getNamespace(GENERIC_NAMESPACE_PREFIX, GENERIC_NAMESPACE_URI);

  protected static final String HTTP_POLICY_NAMESPACE_PREFIX = "http-policy";

  protected static final String POLICY_NAME_ATTR = "policyName";
  protected static final String POLICY_NAME_ATTR_VALUE = "test-policy-name";

  protected MigrationReport reportMock;
  protected ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    reportMock = mock(MigrationReport.class);
    ApplicationModel.ApplicationModelBuilder amb = new ApplicationModel.ApplicationModelBuilder();
    amb.withProjectType(ProjectType.MULE_THREE_POLICY);
    amb.withProjectBasePath(APPLICATION_MODEL_PATH);
    amb.withPom(APPLICATION_MODEL_PATH.resolve("pom.xml"));
    appModel = amb.build();
  }

  protected String getElementNamespaces(final Element element) {
    final StringBuilder sb = new StringBuilder();
    for (final Namespace np : element.getContent().get(0).getNamespacesIntroduced()) {
      sb.append(np.getURI());
    }
    return sb.toString();
  }

  protected void assertProxyAndSourceElements(Element element, int expectedSourceContentSize) {
    assertThat(element.getName(), is(PROXY_TAG_NAME));
    assertThat(element.getNamespacePrefix(), is(HTTP_POLICY_NAMESPACE_PREFIX));
    assertThat(element.getContent().size(), is(1));
    final Element source = (Element) element.getContent(0);
    assertThat(source.getName(), is(SOURCE_TAG_NAME));
    assertThat(source.getNamespacePrefix(), is(HTTP_POLICY_NAMESPACE_PREFIX));
    assertThat(source.getContent().size(), is(expectedSourceContentSize));
  }

  protected void assertExecuteNextElement(Element source, int expectedExecuteNextPosition) {
    final Element executeNext = (Element) source.getContent(expectedExecuteNextPosition);
    assertThat(executeNext.getName(), is(EXECUTE_NEXT_TAG_NAME));
    assertThat(executeNext.getNamespacePrefix(), is(HTTP_POLICY_NAMESPACE_PREFIX));
    assertThat(executeNext.getContent().size(), is(0));
  }

  protected abstract Element getTestElement();

}
