/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.federation;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.CONFIG;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.FEDERATION_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.PINGFEDERATE_GW_NAMESPACE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.mulesoft.tools.migration.library.gateway.steps.policy.federation.PingFederateGwValidateTagMigrationStep;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Test;

public class PingFederateGwValidateTagMigrationStepTestCase extends AbstractFederationTestCase {

  private static final String AUTHENTICATE_PINGFEDERATE_TAG_NAME = "authenticate-ping-federate";
  private static final String PINGFEDERATE_CONFIG_TAG_NAME = "ping-federate-config";
  private static final String HEADERS_AUTHENTICATE_CONTENT =
      "#[{'WWW-Authenticate': 'Bearer realm=\"PingFederate Client Realm\"'}]";

  @Override
  protected Namespace getTestElementNamespace() {
    return PINGFEDERATE_GW_NAMESPACE;
  }

  @Override
  protected String getExpectedAuthenticateTagName() {
    return AUTHENTICATE_PINGFEDERATE_TAG_NAME;
  }

  @Override
  protected String getExpectedHeadersAuthenticateContent() {
    return HEADERS_AUTHENTICATE_CONTENT;
  }

  @Test
  public void assertValidate() {
    final PingFederateGwValidateTagMigrationStep step = new PingFederateGwValidateTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement();

    step.execute(element, reportMock);

    doAsserts(element);
    Element oauthConfig = getRootElement(element).getChild(PINGFEDERATE_CONFIG_TAG_NAME, FEDERATION_NAMESPACE);
    assertThat(oauthConfig, notNullValue());
    assertThat(oauthConfig.getAttributes().size(), is(2));
    assertThat(oauthConfig.getAttributeValue(TOKEN_URL_ATTR_NAME), is(TOKEN_URL_ATTR_VALUE));
    assertThat(oauthConfig.getAttributeValue(NAME_ATTR_NAME), is(CONFIG));
  }

  @Test
  public void assertValidateWithScope() {
    final PingFederateGwValidateTagMigrationStep step = new PingFederateGwValidateTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement()
        .setAttribute(SCOPES_ATTR_NAME, SCOPES_ATTR_VALUE);

    step.execute(element, reportMock);

    doAsserts(element);
    Element oauthConfig = getRootElement(element).getChild(PINGFEDERATE_CONFIG_TAG_NAME, FEDERATION_NAMESPACE);
    assertThat(oauthConfig, notNullValue());
    assertThat(oauthConfig.getAttributes().size(), is(3));
    assertThat(oauthConfig.getAttributeValue(TOKEN_URL_ATTR_NAME), is(TOKEN_URL_ATTR_VALUE));
    assertThat(oauthConfig.getAttributeValue(NAME_ATTR_NAME), is(CONFIG));
    assertThat(oauthConfig.getAttributeValue(SCOPES_ATTR_NAME), is(SCOPES_ATTR_VALUE));
  }

}
