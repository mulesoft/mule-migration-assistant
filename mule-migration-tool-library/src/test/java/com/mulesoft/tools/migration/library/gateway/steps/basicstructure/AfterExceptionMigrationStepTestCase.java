/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.basicstructure;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.ERROR_HANDLER_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_ATTRIBUTE_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_ATTRIBUTE_VALUE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_VALUE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.ON_ERROR_CONTINUE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.TRY_TAG_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.mulesoft.tools.migration.library.gateway.steps.policy.basicstructure.AfterExceptionTagMigrationStep;

import org.jdom2.Element;
import org.junit.Test;

public class AfterExceptionMigrationStepTestCase extends BasicPolicyStructureMigrationTestCase {

  private static final String AFTER_EXCEPTION_TAG_NAME = "after-exception";

  @Override
  protected Element getTestElement() {
    return new Element(AFTER_EXCEPTION_TAG_NAME);
  }

  private void assertErrorHandlerElement(Element tryElement, int expectedOnErrorContinueContentSize) {
    final Element errorHandlerElement = (Element) tryElement.getContent(1);
    assertThat(errorHandlerElement.getName(), is(ERROR_HANDLER_TAG_NAME));
    assertThat(errorHandlerElement.getContent().size(), is(1));
    final Element onErrorContinueElement = (Element) errorHandlerElement.getContent(0);
    assertThat(onErrorContinueElement.getName(), is(ON_ERROR_CONTINUE_TAG_NAME));
    assertThat(onErrorContinueElement.getContent().size(), is(expectedOnErrorContinueContentSize));
  }

  @Test
  public void convertRawBeforeTag() {
    final AfterExceptionTagMigrationStep step = new AfterExceptionTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement();

    step.execute(element, reportMock);

    assertProxyAndSourceElements(element, 1);
    final Element source = (Element) element.getContent(0);
    final Element tryElement = (Element) source.getContent(0);
    assertThat(tryElement.getName(), is(TRY_TAG_NAME));
    assertThat(tryElement.getContent().size(), is(2));
    assertExecuteNextElement(tryElement, 0);
    assertErrorHandlerElement(tryElement, 0);
  }

  @Test
  public void convertBeforeTagWithContent() {
    final AfterExceptionTagMigrationStep step = new AfterExceptionTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement().addContent(new Element(GENERIC_TAG_NAME)
        .setText(GENERIC_TAG_VALUE)
        .setAttribute(GENERIC_TAG_ATTRIBUTE_NAME, GENERIC_TAG_ATTRIBUTE_VALUE));

    step.execute(element, reportMock);

    assertProxyAndSourceElements(element, 1);
    final Element source = (Element) element.getContent(0);
    final Element tryElement = (Element) source.getContent(0);
    assertThat(tryElement.getName(), is(TRY_TAG_NAME));
    assertThat(tryElement.getContent().size(), is(2));
    assertExecuteNextElement(tryElement, 0);
    assertErrorHandlerElement(tryElement, 1);
    final Element errorHandlerElement = (Element) tryElement.getContent(1);
    final Element onErrorContinueElement = (Element) errorHandlerElement.getContent(0);
    final Element contentElement = (Element) onErrorContinueElement.getContent(0);
    assertThat(contentElement.getParentElement().getName(), is(ON_ERROR_CONTINUE_TAG_NAME));
    assertThat(contentElement.getValue(), is(GENERIC_TAG_VALUE));

  }
}
