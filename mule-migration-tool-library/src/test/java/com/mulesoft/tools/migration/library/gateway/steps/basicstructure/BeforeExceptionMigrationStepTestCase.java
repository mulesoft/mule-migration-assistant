/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.basicstructure;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_ATTRIBUTE_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_ATTRIBUTE_VALUE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_VALUE;

import com.mulesoft.tools.migration.library.gateway.steps.policy.basicstructure.BeforeExceptionTagMigrationStep;

import org.jdom2.Element;
import org.junit.Test;

public class BeforeExceptionMigrationStepTestCase extends BasicPolicyStructureMigrationTestCase {

  private static final String BEFORE_EXCEPTION_TAG_NAME = "before-exception";

  @Override
  protected Element getTestElement() {
    return new Element(BEFORE_EXCEPTION_TAG_NAME);
  }

  @Test
  public void convertRawBeforeTag() {
    final BeforeExceptionTagMigrationStep step = new BeforeExceptionTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement();

    step.execute(element, reportMock);

    assertProxyAndSourceElements(element, 1);
    assertExecuteNextElement((Element) element.getContent(0), 0);
  }

  @Test
  public void convertBeforeTagWithContent() {
    final BeforeExceptionTagMigrationStep step = new BeforeExceptionTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement().addContent(new Element(GENERIC_TAG_NAME)
        .setText(GENERIC_TAG_VALUE)
        .setAttribute(GENERIC_TAG_ATTRIBUTE_NAME, GENERIC_TAG_ATTRIBUTE_VALUE));

    step.execute(element, reportMock);

    assertProxyAndSourceElements(element, 1);
    assertExecuteNextElement((Element) element.getContent(0), 0);
  }
}
