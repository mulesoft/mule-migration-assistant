/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.threatprotection;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.ON_UNACCEPTED;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.POLICY_ID_ATTR_VALUE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.JSON_THREAT_PROTECTION_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_CORE_NAMESPACE_NO_PREFIX;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.THREAT_PROTECTION_GW_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.XML_THREAT_PROTECTION_NAMESPACE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.library.gateway.steps.policy.threatprotection.ProtectTagMigrationStep;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

public class ProtectTagMigrationStepTestCase {

  private static final String MULE_4_TAG_NAME = "mule";
  private static final String PROTECT = "protect";
  private static final String JSON_CONFIG = "json-config";
  private static final String XML_CONFIG = "xml-config";
  private static final String SECURE_XML_REQUEST = "secure-xml-request";
  private static final String SECURE_JSON_REQUEST = "secure-json-request";

  private static final String THREAT_PROTECTION_POLICY_REF = "threat-protection-policy-ref";
  private static final String POLICY_ID_BUILD_RESPONSE = "{{policyId}}-build-response";

  private static final String CONFIG_REF = "config-ref";
  private static final String JSON_THREAT_PROTECTION_CONFIG = "json-threat-protection-config";
  private static final String XML_THREAT_PROTECTION_CONFIG = "xml-threat-protection-config";
  private static final String CONTENT_TYPE = "contentType";
  private static final String CONTENT_TYPE_VALUE = "#[attributes.headers['content-type']]";

  private MigrationReport reportMock;
  private ApplicationModel appModel;


  @Before
  public void setUp() throws Exception {
    reportMock = mock(MigrationReport.class);
    appModel = mock(ApplicationModel.class);
  }

  private Element getMuleElement(boolean isJson) {
    Document doc = new Document();
    Element element = new Element(MULE_4_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX)
        .addContent(new Element(isJson ? JSON_CONFIG : XML_CONFIG,
                                isJson ? JSON_THREAT_PROTECTION_NAMESPACE : XML_THREAT_PROTECTION_NAMESPACE));
    doc.setRootElement(element);
    return element;
  }


  private Element getTestElement(boolean isJson) {
    Element element = new Element(PROTECT, THREAT_PROTECTION_GW_NAMESPACE)
        .setAttribute(THREAT_PROTECTION_POLICY_REF, POLICY_ID_ATTR_VALUE)
        .setAttribute(ON_UNACCEPTED, POLICY_ID_BUILD_RESPONSE);
    getMuleElement(isJson).addContent(element);
    return element;
  }

  @Test
  public void convertJsonProtectTag() {
    ProtectTagMigrationStep step = new ProtectTagMigrationStep();
    Element element = getTestElement(true);

    step.execute(element, reportMock);

    assertThat(element.getName(), is(SECURE_JSON_REQUEST));
    assertThat(element.getNamespace(), is(JSON_THREAT_PROTECTION_NAMESPACE));
    assertThat(element.getAttributes().size(), is(2));
    assertThat(element.getAttribute(CONFIG_REF).getValue(), is(JSON_THREAT_PROTECTION_CONFIG));
    assertThat(element.getAttribute(CONTENT_TYPE).getValue(), is(CONTENT_TYPE_VALUE));
    assertThat(element.getContentSize(), is(0));
  }

  @Test
  public void convertXmlProtectTag() {
    ProtectTagMigrationStep step = new ProtectTagMigrationStep();
    Element element = getTestElement(false);

    step.execute(element, reportMock);

    assertThat(element.getName(), is(SECURE_XML_REQUEST));
    assertThat(element.getNamespace(), is(XML_THREAT_PROTECTION_NAMESPACE));
    assertThat(element.getAttributes().size(), is(2));
    assertThat(element.getAttribute(CONFIG_REF).getValue(), is(XML_THREAT_PROTECTION_CONFIG));
    assertThat(element.getAttribute(CONTENT_TYPE).getValue(), is(CONTENT_TYPE_VALUE));
    assertThat(element.getContentSize(), is(0));
  }

}
