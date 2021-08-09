/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.ipfilter;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.IP_FILTER_GW_NAMESPACE;

import com.mulesoft.tools.migration.library.gateway.steps.policy.ipfilter.WhitelistTagMigrationStep;

import org.jdom2.Element;
import org.jdom2.Text;
import org.junit.Test;

public class WhitelistTagMigrationStepTestCase extends AbstractIpFilterMigrationTestCase {

  private static final String WHITELIST_TAG_NAME = "whitelist";
  private static final String WHITELIST_CONFIG_REF_ATTR_VALUE = "whitelist_config";

  @Override
  protected Element getTestElement() {
    return new Element(WHITELIST_TAG_NAME, IP_FILTER_GW_NAMESPACE);
  }

  private void assertWhitelistTag(Element element) {
    assertBlacklistWhitelistTag(element, WHITELIST_TAG_NAME, WHITELIST_CONFIG_REF_ATTR_VALUE);
  }

  @Test
  public void convertRawWhitelistTag() {
    WhitelistTagMigrationStep step = new WhitelistTagMigrationStep();
    Element element = getTestElement();

    step.execute(element, reportMock);

    assertWhitelistTag(element);
  }

  @Test
  public void convertWhitelistTagWithContent() {
    WhitelistTagMigrationStep step = new WhitelistTagMigrationStep();
    Element element = getTestElement()
        .addContent(new Text(IPS_CONTENT_OPEN_TEXT))
        .addContent(new Element(GENERIC_TAG_NAME))
        .addContent(new Text(IPS_CONTENT_CLOSE_TEXT));

    step.execute(element, reportMock);

    assertWhitelistTag(element);
  }

}
