/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.ipfilter;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.IP_FILTER_GW_NAMESPACE;

import com.mulesoft.tools.migration.library.gateway.steps.policy.ipfilter.BlacklistTagMigrationStep;

import org.jdom2.Element;
import org.jdom2.Text;
import org.junit.Test;

public class BlacklistTagMigrationStepTestCase extends AbstractIpFilterMigrationTestCase {

  private static final String BLACKLIST_TAG_NAME = "blacklist";
  private static final String BLACKLIST_CONFIG_REF_ATTR_VALUE = "blacklist_config";

  @Override
  protected Element getTestElement() {
    return new Element(BLACKLIST_TAG_NAME, IP_FILTER_GW_NAMESPACE);
  }

  private void assertBlacklistTag(Element element) {
    assertBlacklistWhitelistTag(element, BLACKLIST_TAG_NAME, BLACKLIST_CONFIG_REF_ATTR_VALUE);
  }

  @Test
  public void convertRawBlacklistTag() {
    BlacklistTagMigrationStep step = new BlacklistTagMigrationStep();
    Element element = getTestElement();

    step.execute(element, reportMock);

    assertBlacklistTag(element);
  }

  @Test
  public void convertBlacklistTagWithContent() {
    BlacklistTagMigrationStep step = new BlacklistTagMigrationStep();
    Element element = getTestElement()
        .addContent(new Text(IPS_CONTENT_OPEN_TEXT))
        .addContent(new Element(GENERIC_TAG_NAME))
        .addContent(new Text(IPS_CONTENT_CLOSE_TEXT));

    step.execute(element, reportMock);

    assertBlacklistTag(element);
  }

}
