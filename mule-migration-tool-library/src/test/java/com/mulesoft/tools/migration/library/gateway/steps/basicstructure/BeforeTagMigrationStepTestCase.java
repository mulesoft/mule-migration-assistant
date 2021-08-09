/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.basicstructure;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.BEFORE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_ATTRIBUTE_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_ATTRIBUTE_VALUE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_VALUE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.SOURCE_TAG_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;

import com.mulesoft.tools.migration.library.gateway.steps.policy.basicstructure.BeforeTagMigrationStep;

import org.jdom2.Element;
import org.junit.Test;

public class BeforeTagMigrationStepTestCase extends BasicPolicyStructureMigrationTestCase {

  @Override
  protected Element getTestElement() {
    return new Element(BEFORE_TAG_NAME);
  }

  @Test
  public void convertRawBeforeTag() {
    final BeforeTagMigrationStep step = new BeforeTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement();

    step.execute(element, reportMock);

    assertProxyAndSourceElements(element, 1);
    assertExecuteNextElement((Element) element.getContent(0), 0);
  }

  @Test
  public void convertBeforeTagWithContent() {
    final BeforeTagMigrationStep step = new BeforeTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement().addContent(new Element(GENERIC_TAG_NAME)
        .setText(GENERIC_TAG_VALUE)
        .setAttribute(GENERIC_TAG_ATTRIBUTE_NAME, GENERIC_TAG_ATTRIBUTE_VALUE));

    step.execute(element, reportMock);

    assertProxyAndSourceElements(element, 2);
    final Element source = (Element) element.getContent(0);
    final Element contentElement = (Element) source.getContent(0);
    assertThat(contentElement.getParentElement().getName(), is(SOURCE_TAG_NAME));
    assertThat(contentElement.getParentElement().getNamespacePrefix(), is(HTTP_POLICY_NAMESPACE_PREFIX));
    assertThat(contentElement.getValue(), is(GENERIC_TAG_VALUE));
    assertThat(contentElement.getCType().name(), is(Element.class.getSimpleName()));
    assertThat(contentElement.getNamespacesIntroduced().size(), is(1));
    assertExecuteNextElement((Element) element.getContent(0), 1);
  }

  @Test
  public void setDefaultPolicyName() {
    final BeforeTagMigrationStep step = new BeforeTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement();

    step.execute(element, reportMock);

    assertThat(element.getAttributeValue(NAME), is("basic-structure-policy"));
    verify(reportMock).report("basicStructure.defaultPolicyName", element, element, "basic-structure-policy");
  }

}
