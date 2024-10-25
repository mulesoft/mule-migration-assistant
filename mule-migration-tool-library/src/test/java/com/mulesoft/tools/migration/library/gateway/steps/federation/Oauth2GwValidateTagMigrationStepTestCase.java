/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.federation;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.CONFIG;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.FEDERATION_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.OAUTH2_GW_NAMESPACE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.mulesoft.tools.migration.library.gateway.steps.policy.federation.Oauth2GwValidateTagMigrationStep;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Test;

public class Oauth2GwValidateTagMigrationStepTestCase extends AbstractFederationTestCase {

  private static final String AUTHENTICATE_OAUTH2_TAG_NAME = "authenticate-oauth2";
  private static final String OAUTH2_CONFIG_TAG_NAME = "oauth2-config";
  private static final String HEADERS_AUTHENTICATE_CONTENT = "#[{'WWW-Authenticate': 'Bearer realm=\"OAuth2 Client Realm\"'}]";

  @Override
  protected Namespace getTestElementNamespace() {
    return OAUTH2_GW_NAMESPACE;
  }

  @Override
  protected String getExpectedAuthenticateTagName() {
    return AUTHENTICATE_OAUTH2_TAG_NAME;
  }

  @Override
  protected String getExpectedHeadersAuthenticateContent() {
    return HEADERS_AUTHENTICATE_CONTENT;
  }

  @Test
  public void assertValidate() {
    final Oauth2GwValidateTagMigrationStep step = new Oauth2GwValidateTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement();

    step.execute(element, reportMock);

    doAsserts(element);
    Element oauthConfig = getRootElement(element).getChild(OAUTH2_CONFIG_TAG_NAME, FEDERATION_NAMESPACE);
    assertThat(oauthConfig, notNullValue());
    assertThat(oauthConfig.getAttributes().size(), is(2));
    assertThat(oauthConfig.getAttributeValue(TOKEN_URL_ATTR_NAME), is(TOKEN_URL_ATTR_VALUE));
    assertThat(oauthConfig.getAttributeValue(NAME_ATTR_NAME), is(CONFIG));
  }

  @Test
  public void assertValidateWithScope() {
    final Oauth2GwValidateTagMigrationStep step = new Oauth2GwValidateTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement()
        .setAttribute(SCOPES_ATTR_NAME, SCOPES_ATTR_VALUE);

    step.execute(element, reportMock);

    doAsserts(element);
    Element oauthConfig = getRootElement(element).getChild(OAUTH2_CONFIG_TAG_NAME, FEDERATION_NAMESPACE);
    assertThat(oauthConfig, notNullValue());
    assertThat(oauthConfig.getAttributes().size(), is(3));
    assertThat(oauthConfig.getAttributeValue(TOKEN_URL_ATTR_NAME), is(TOKEN_URL_ATTR_VALUE));
    assertThat(oauthConfig.getAttributeValue(NAME_ATTR_NAME), is(CONFIG));
    assertThat(oauthConfig.getAttributeValue(SCOPES_ATTR_NAME), is(SCOPES_ATTR_VALUE));
  }

}
