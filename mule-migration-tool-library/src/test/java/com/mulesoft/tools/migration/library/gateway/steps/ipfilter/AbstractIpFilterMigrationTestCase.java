/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.ipfilter;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.IP_FILTER_NAMESPACE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jdom2.Element;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractIpFilterMigrationTestCase {

  protected static final String IP_ADDRESS_ATTR_NAME = "ipAddress";
  protected static final String IP_ADDRESS_ATTR_VALUE = "{{ipExpression}}";
  protected static final String CONFIG_REF_ATTR_NAME = "config-ref";

  protected static final String IPS_CONTENT_OPEN_TEXT = "{{#ips}}";
  protected static final String IPS_CONTENT_CLOSE_TEXT = "{{/ips}}";

  protected static final String GENERIC_TAG_NAME = "foo";

  protected MigrationReport reportMock;
  protected ApplicationModel appModel;

  protected static final Path APPLICATION_MODEL_PATH = Paths.get("src/test/resources/mule/apps/gateway/ip-filter/expected");
  protected static final Path MIGRATION_RESOURCES_PATH = Paths.get("src/main/resources/migration");

  @Before
  public void setUp() throws Exception {
    reportMock = mock(MigrationReport.class);
    ApplicationModel.ApplicationModelBuilder amb = new ApplicationModel.ApplicationModelBuilder();
    amb.withProjectType(ProjectType.MULE_THREE_POLICY);
    amb.withProjectBasePath(APPLICATION_MODEL_PATH);
    appModel = amb.build();
  }

  @After
  public void cleanup() throws Exception {
    File dwFile = APPLICATION_MODEL_PATH.resolve(MIGRATION_RESOURCES_PATH).resolve("HttpListener.dwl").toFile();
    if (!dwFile.delete()) {
      dwFile.deleteOnExit();
    }
  }

  protected void assertBlacklistWhitelistTag(Element element, String tagName, String configRefAttrValue) {
    assertThat(element.getName(), is(tagName));
    assertThat(element.getNamespace(), is(IP_FILTER_NAMESPACE));
    assertThat(element.getAttributeValue(CONFIG_REF_ATTR_NAME), is(configRefAttrValue));
    assertThat(element.getContentSize(), is(0));
  }

  protected abstract Element getTestElement();
}
