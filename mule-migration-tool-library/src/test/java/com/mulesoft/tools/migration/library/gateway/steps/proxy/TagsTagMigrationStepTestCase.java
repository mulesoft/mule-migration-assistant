/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.proxy;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_ATTRIBUTE_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_ATTRIBUTE_VALUE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERIC_TAG_VALUE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.API_PLATFORM_GW_MULE_3_NAMESPACE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

public class TagsTagMigrationStepTestCase {

  private static final String TAGS_TAG_NAME = "tags";

  protected MigrationReport reportMock;

  @Before
  public void setUp() throws Exception {
    reportMock = mock(MigrationReport.class);
  }

  protected Element getTestElement() {
    return new Element(TAGS_TAG_NAME, API_PLATFORM_GW_MULE_3_NAMESPACE);
  }

  @Test
  public void convertRawTagsTag() {
    final TagsTagMigrationStep step = new TagsTagMigrationStep();
    Element element = getTestElement();

    step.execute(element, reportMock);

    assertThat(element.getParent(), nullValue());
    assertThat(element.getContentSize(), is(0));
    verify(reportMock).report("proxy.tagsTagMigrationStep", element, element);
  }

  @Test
  public void convertTagsTagWithContent() {
    final TagsTagMigrationStep step = new TagsTagMigrationStep();
    Element element = getTestElement().addContent(new Element(GENERIC_TAG_NAME)
        .setText(GENERIC_TAG_VALUE)
        .setAttribute(GENERIC_TAG_ATTRIBUTE_NAME, GENERIC_TAG_ATTRIBUTE_VALUE));

    step.execute(element, reportMock);

    assertThat(element.getParent(), nullValue());
    assertThat(element.getContentSize(), is(0));
    verify(reportMock).report("proxy.tagsTagMigrationStep", element, element);
  }
}
