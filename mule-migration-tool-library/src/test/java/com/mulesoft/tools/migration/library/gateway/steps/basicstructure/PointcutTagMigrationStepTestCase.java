/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.basicstructure;

import static org.mockito.Mockito.verify;

import com.mulesoft.tools.migration.library.gateway.steps.policy.basicstructure.PointcutTagMigrationStep;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Test;

public class PointcutTagMigrationStepTestCase extends BasicPolicyStructureMigrationTestCase {

  private static final String POINTCUT_TAG_NAME = "pointcut";

  private static final String API_POINTCUT_TAG_NAME = "api-pointcut";
  private static final Namespace API_PLATFORM_GW_NAMESPACE =
      Namespace.getNamespace("api-platform-gw", "http://www.mulesoft.org/schema/mule/api-platform-gw");
  private static final String API_NAME_ATTR_NAME = "apiName";
  private static final String API_NAME_ATTR_VALUE = "{{apiName}}";
  private static final String API_VERSION_ATTR_NAME = "apiVersion";
  private static final String API_VERSION_ATTR_VALUE = "{{apiVersionName}}";

  private static final String RESOURCE_TAG_NAME = "resource";
  private static final String METHOD_REGEX_ATTR_NAME = "methodRegex";
  private static final String METHOD_REGEX_ATTR_VALUE = "{{methodRegex}}";
  private static final String URI_TEMPLATE_REGEX_ATTR_NAME = "uriTemplateRegex";
  private static final String URI_TEMPLATE_REGEX_ATTR_VALUE = "{{uriTemplateRegex}}";

  private static final String ENDPOINT_TAG_NAME = "endpoint";
  private static final String APP_TAG_NAME = "app";
  private static final String REGEX_ATTR_NAME = "regex";
  private static final String REGEX_ATTR_VALUE = ".*";

  @Override
  protected Element getTestElement() {
    return new Element(POINTCUT_TAG_NAME);
  }

  @Test
  public void convertRawPointcutTag() {
    final PointcutTagMigrationStep step = new PointcutTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement();

    step.execute(element, reportMock);

    assertProxyAndSourceElements(element, 1);
    assertExecuteNextElement((Element) element.getContent(0), 0);
    verify(reportMock).report("basicStructure.pointcutMigrationStepUnknown", element, element, "Unknown");
  }

  @Test
  public void convertApiPlatformPoinctuTag() {
    final PointcutTagMigrationStep step = new PointcutTagMigrationStep();
    step.setApplicationModel(appModel);
    Element apiPointcut = new Element(API_POINTCUT_TAG_NAME, API_PLATFORM_GW_NAMESPACE)
        .setAttribute(API_NAME_ATTR_NAME, API_NAME_ATTR_VALUE).setAttribute(API_VERSION_ATTR_NAME, API_VERSION_ATTR_VALUE);
    Element element = getTestElement().addContent(apiPointcut);

    step.execute(element, reportMock);

    assertProxyAndSourceElements(element, 1);
    assertExecuteNextElement((Element) element.getContent(0), 0);
    verify(reportMock).report("basicStructure.pointcutMigrationStepResolvedByRuntime", apiPointcut, apiPointcut, "Api");
  }

  @Test
  public void convertResourcePoinctuTag() {
    final PointcutTagMigrationStep step = new PointcutTagMigrationStep();
    step.setApplicationModel(appModel);
    Element apiPointcut = new Element(API_POINTCUT_TAG_NAME, API_PLATFORM_GW_NAMESPACE)
        .setAttribute(API_NAME_ATTR_NAME, API_NAME_ATTR_VALUE).setAttribute(API_VERSION_ATTR_NAME, API_VERSION_ATTR_VALUE);
    Element resource = new Element(RESOURCE_TAG_NAME)
        .setAttribute(METHOD_REGEX_ATTR_NAME, METHOD_REGEX_ATTR_VALUE)
        .setAttribute(URI_TEMPLATE_REGEX_ATTR_NAME, URI_TEMPLATE_REGEX_ATTR_VALUE);
    Element element = getTestElement().addContent(apiPointcut)
        .addContent(resource);

    step.execute(element, reportMock);

    assertProxyAndSourceElements(element, 1);
    assertExecuteNextElement((Element) element.getContent(0), 0);
    verify(reportMock).report("basicStructure.pointcutMigrationStepResolvedByRuntime", apiPointcut, apiPointcut, "Api");
    verify(reportMock).report("basicStructure.pointcutMigrationStepResolvedByRuntime", resource, resource, "Resource");
  }

  @Test
  public void convertAppPoinctuTag() {
    final PointcutTagMigrationStep step = new PointcutTagMigrationStep();
    step.setApplicationModel(appModel);
    Element app = new Element(APP_TAG_NAME)
        .setAttribute(REGEX_ATTR_NAME, REGEX_ATTR_VALUE);
    Element element = getTestElement().addContent(app);

    step.execute(element, reportMock);

    assertProxyAndSourceElements(element, 1);
    assertExecuteNextElement((Element) element.getContent(0), 0);
    verify(reportMock).report("basicStructure.pointcutMigrationStepNoEquivalent", app, app, "App");
  }

  @Test
  public void convertEndpointPoinctuTag() {
    final PointcutTagMigrationStep step = new PointcutTagMigrationStep();
    step.setApplicationModel(appModel);
    Element endpoint = new Element(ENDPOINT_TAG_NAME)
        .setAttribute(REGEX_ATTR_NAME, REGEX_ATTR_VALUE);
    Element element = getTestElement().addContent(endpoint);

    step.execute(element, reportMock);

    assertProxyAndSourceElements(element, 1);
    assertExecuteNextElement((Element) element.getContent(0), 0);
    verify(reportMock).report("basicStructure.pointcutMigrationStepNoEquivalent", endpoint, endpoint, "Endpoint");
  }
}
