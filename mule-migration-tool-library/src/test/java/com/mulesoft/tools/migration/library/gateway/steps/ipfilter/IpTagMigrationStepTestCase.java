/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.ipfilter;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.IP_FILTER_GW_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.IP_FILTER_NAMESPACE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.mulesoft.tools.migration.library.gateway.steps.policy.ipfilter.IpTagMigrationStep;

import org.jdom2.Element;
import org.jdom2.Text;
import org.junit.Test;

public class IpTagMigrationStepTestCase extends AbstractIpFilterMigrationTestCase {

  private static final String IP_TAG_NAME = "ip";
  private static final String VALUE_ATTR_NAME = "value";
  private static final String VALUE_ATTR_VALUE = "{{.}}";

  @Override
  protected Element getTestElement() {
    return new Element(IP_TAG_NAME, IP_FILTER_GW_NAMESPACE);
  }

  private void assertIpTag(Element element) {
    assertThat(element.getName(), is(IP_TAG_NAME));
    assertThat(element.getNamespace(), is(IP_FILTER_NAMESPACE));
    assertThat(element.getContentSize(), is(0));
  }

  @Test
  public void convertRawIpTag() {
    IpTagMigrationStep step = new IpTagMigrationStep();
    Element element = getTestElement();

    step.execute(element, reportMock);

    assertIpTag(element);
  }

  @Test
  public void convertIpTagWithContent() {
    IpTagMigrationStep step = new IpTagMigrationStep();
    Element element = getTestElement()
        .addContent(new Text(VALUE_ATTR_VALUE));

    step.execute(element, reportMock);

    assertIpTag(element);
    assertThat(element.getAttributeValue(VALUE_ATTR_NAME), is(VALUE_ATTR_VALUE));
  }

}
