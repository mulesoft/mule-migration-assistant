/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.threatprotection;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.POLICY_ID_ATTR_VALUE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.JSON_THREAT_PROTECTION_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.THREAT_PROTECTION_GW_NAMESPACE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.library.gateway.steps.policy.threatprotection.JsonPolicyTagMigrationStep;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

public class JsonPolicyTagMigrationStepTestCase {

  private static final String JSON_POLICY = "json-policy";
  private static final String JSON_CONFIG = "json-config";

  private static final String MAX_CONTAINER_DEPTH = "maxContainerDepth";
  private static final String MAX_STRING_VALUE_LENGTH = "maxStringValueLength";
  private static final String MAX_OBJECT_ENTRY_NAME_LENGTH = "maxObjectEntryNameLength";
  private static final String MAX_OBJECT_ENTRY_COUNT = "maxObjectEntryCount";
  private static final String MAX_ARRAY_ELEMENT_COUNT = "maxArrayElementCount";
  private static final String NAME = "name";

  private static final String MAX_CONTAINER_DEPTH_VALUE = "{{maxContainerDepth}}";
  private static final String MAX_STRING_VALUE_LENGTH_VALUE = "{{maxStringValueLength}}";
  private static final String MAX_OBJECT_ENTRY_NAME_LENGTH_VALUE = "{{maxObjectEntryNameLength}}";
  private static final String MAX_OBJECT_ENTRY_COUNT_VALUE = "{{maxObjectEntryCount}}";
  private static final String MAX_ARRAY_ELEMENT_COUNT_VALUE = "{{maxArrayElementCount}}";
  private static final String NAME_VALUE = "json-threat-protection-config";

  private static final String COM_MULESOFT_ANYPOINT_GROUP_ID = "com.mulesoft.anypoint";
  private static final String MULE_JSON_THREAT_PROTECTION_EXTENSION_ARTIFACT_ID = "mule-json-threat-protection-extension";
  private static final String JSON_THREAT_PROTECTION_EXTENSION_VERSION = "1.1.0";
  private static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";

  private static final String XPATH_NODE_EXPRESSION =
      "//*[namespace-uri() = 'http://www.mulesoft.org/schema/mule/threat-protection-gw' and local-name() = 'json-policy']";

  private static final Path APPLICATION_MODEL_PATH = Paths.get("src/test/resources/mule/apps/gateway/threat-protection/original");

  private MigrationReport reportMock;
  private ApplicationModel appModel;


  @Before
  public void setUp() throws Exception {
    reportMock = mock(MigrationReport.class);
    ApplicationModel.ApplicationModelBuilder appModelBuilder = new ApplicationModel.ApplicationModelBuilder();
    appModelBuilder.withProjectType(ProjectType.MULE_THREE_POLICY);
    appModelBuilder.withProjectBasePath(APPLICATION_MODEL_PATH);
    appModelBuilder.withConfigurationFiles(Arrays.asList(APPLICATION_MODEL_PATH.resolve("json-threat-protection3.xml")));
    appModel = appModelBuilder.build();
  }

  private Element getTestElement() {
    return new Element(JSON_POLICY, THREAT_PROTECTION_GW_NAMESPACE)
        .setAttribute(ID, POLICY_ID_ATTR_VALUE)
        .setAttribute(MAX_CONTAINER_DEPTH, MAX_CONTAINER_DEPTH_VALUE)
        .setAttribute(MAX_STRING_VALUE_LENGTH, MAX_STRING_VALUE_LENGTH_VALUE)
        .setAttribute(MAX_OBJECT_ENTRY_NAME_LENGTH, MAX_OBJECT_ENTRY_NAME_LENGTH_VALUE)
        .setAttribute(MAX_OBJECT_ENTRY_COUNT, MAX_OBJECT_ENTRY_COUNT_VALUE)
        .setAttribute(MAX_ARRAY_ELEMENT_COUNT, MAX_ARRAY_ELEMENT_COUNT_VALUE);
  }

  private void assertJsonPolicyTag(Element element) {
    assertThat(element.getName(), is(JSON_CONFIG));
    assertThat(element.getNamespace(), is(JSON_THREAT_PROTECTION_NAMESPACE));
    assertThat(element.getAttributes().size(), is(6));
    assertThat(element.getAttribute(MAX_CONTAINER_DEPTH).getValue(), is(MAX_CONTAINER_DEPTH_VALUE));
    assertThat(element.getAttribute(MAX_STRING_VALUE_LENGTH).getValue(), is(MAX_STRING_VALUE_LENGTH_VALUE));
    assertThat(element.getAttribute(MAX_OBJECT_ENTRY_NAME_LENGTH).getValue(), is(MAX_OBJECT_ENTRY_NAME_LENGTH_VALUE));
    assertThat(element.getAttribute(MAX_OBJECT_ENTRY_COUNT).getValue(), is(MAX_OBJECT_ENTRY_COUNT_VALUE));
    assertThat(element.getAttribute(MAX_ARRAY_ELEMENT_COUNT).getValue(), is(MAX_ARRAY_ELEMENT_COUNT_VALUE));
    assertThat(element.getAttribute(NAME).getValue(), is(NAME_VALUE));
  }

  @Test
  public void convertJsonPolicyTag() {
    JsonPolicyTagMigrationStep step = new JsonPolicyTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement();

    step.execute(element, reportMock);

    assertJsonPolicyTag(element);
  }

  @Test
  public void jsonPomContributionTest() {
    JsonPolicyTagMigrationStep step = new JsonPolicyTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = appModel.getNode(XPATH_NODE_EXPRESSION);

    step.execute(element, reportMock);

    PomModel pm = appModel.getPomModel().get();

    assertThat(pm.getDependencies().size(), is(1));
    Dependency jsonThreatProtectionDependency = pm.getDependencies().get(0);
    assertThat(jsonThreatProtectionDependency.getGroupId(), is(COM_MULESOFT_ANYPOINT_GROUP_ID));
    assertThat(jsonThreatProtectionDependency.getArtifactId(), is(MULE_JSON_THREAT_PROTECTION_EXTENSION_ARTIFACT_ID));
    assertThat(jsonThreatProtectionDependency.getVersion(), is(JSON_THREAT_PROTECTION_EXTENSION_VERSION));
    assertThat(jsonThreatProtectionDependency.getClassifier(), is(MULE_PLUGIN_CLASSIFIER));

    assertJsonPolicyTag(element);
  }

}
