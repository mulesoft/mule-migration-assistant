/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.threatprotection;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.POLICY_ID_ATTR_VALUE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.THREAT_PROTECTION_GW_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.XML_THREAT_PROTECTION_NAMESPACE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.mulesoft.tools.migration.library.gateway.steps.policy.threatprotection.XmlPolicyTagMigrationStep;
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

public class XmlPolicyTagMigrationStepTestCase {

  private static final String XML_POLICY = "xml-policy";
  private static final String STRUCTURE = "structure";
  private static final String VALUES = "values";
  private static final String XML_CONFIG = "xml-config";

  private static final String MAX_NODE_DEPTH = "maxNodeDepth";
  private static final String MAX_ATTRIBUTE_COUNT_PER_ELEMENT = "maxAttributeCountPerElement";
  private static final String MAX_CHILD_COUNT = "maxChildCount";
  private static final String MAX_TEXT_LENGTH = "maxTextLength";
  private static final String MAX_ATTRIBUTE_LENGTH = "maxAttributeLength";
  private static final String MAX_COMMENT_LENGTH = "maxCommentLength";
  private static final String NAME = "name";

  private static final String MAX_NODE_DEPTH_VALUE = "{{maxNodeDepth}}";
  private static final String MAX_ATTRIBUTE_COUNT_PER_ELEMENT_VALUE = "{{maxAttributeCountPerElement}}";
  private static final String MAX_CHILD_COUNT_VALUE = "{{maxChildCount}}";
  private static final String MAX_TEXT_LENGTH_VALUE = "{{maxTextLength}}";
  private static final String MAX_ATTRIBUTE_LENGTH_VALUE = "{{maxAttributeLength}}";
  private static final String MAX_COMMENT_LENGTH_VALUE = "{{maxCommentLength}}";
  private static final String NAME_VALUE = "xml-threat-protection-config";


  private static final String COM_MULESOFT_ANYPOINT_GROUP_ID = "com.mulesoft.anypoint";
  private static final String MULE_XML_THREAT_PROTECTION_EXTENSION_ARTIFACT_ID = "mule-xml-threat-protection-extension";
  private static final String XML_JSON_THREAT_PROTECTION_EXTENSION_VERSION = "1.1.0";
  private static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";

  private static final String XPATH_NODE_EXPRESSION =
      "//*[namespace-uri() = 'http://www.mulesoft.org/schema/mule/threat-protection-gw' and local-name() = 'xml-policy']";

  private static final Path APPLICATION_MODEL_PATH = Paths.get("src/test/resources/mule/apps/gateway/threat-protection/original");

  private MigrationReport reportMock;
  private ApplicationModel appModel;


  @Before
  public void setUp() throws Exception {
    reportMock = mock(MigrationReport.class);
    ApplicationModel.ApplicationModelBuilder appModelBuilder = new ApplicationModel.ApplicationModelBuilder();
    appModelBuilder.withProjectType(ProjectType.MULE_THREE_POLICY);
    appModelBuilder.withProjectBasePath(APPLICATION_MODEL_PATH);
    appModelBuilder.withConfigurationFiles(Arrays.asList(APPLICATION_MODEL_PATH.resolve("xml-threat-protection3.xml")));
    appModel = appModelBuilder.build();
  }

  private Element getValuesElement() {
    return new Element(VALUES, THREAT_PROTECTION_GW_NAMESPACE)
        .setAttribute(MAX_TEXT_LENGTH, MAX_TEXT_LENGTH_VALUE)
        .setAttribute(MAX_ATTRIBUTE_LENGTH, MAX_ATTRIBUTE_LENGTH_VALUE)
        .setAttribute(MAX_COMMENT_LENGTH, MAX_COMMENT_LENGTH_VALUE);
  }

  private Element getStructureElement() {
    return new Element(STRUCTURE, THREAT_PROTECTION_GW_NAMESPACE)
        .setAttribute(MAX_NODE_DEPTH, MAX_NODE_DEPTH_VALUE)
        .setAttribute(MAX_ATTRIBUTE_COUNT_PER_ELEMENT, MAX_ATTRIBUTE_COUNT_PER_ELEMENT_VALUE)
        .setAttribute(MAX_CHILD_COUNT, MAX_CHILD_COUNT_VALUE);
  }

  private Element getTestElement() {
    return new Element(XML_POLICY, THREAT_PROTECTION_GW_NAMESPACE)
        .setAttribute(ID, POLICY_ID_ATTR_VALUE);
  }

  @Test
  public void convertRawXmlPolicyTag() {
    XmlPolicyTagMigrationStep step = new XmlPolicyTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement();

    step.execute(element, reportMock);

    assertThat(element.getName(), is(XML_CONFIG));
    assertThat(element.getNamespace(), is(XML_THREAT_PROTECTION_NAMESPACE));
    assertThat(element.getAttributes().size(), is(1));
    assertThat(element.getAttribute(NAME).getValue(), is(NAME_VALUE));
    assertThat(element.getContentSize(), is(0));
    verify(reportMock).report("threatProtection.missingElement", element, element, STRUCTURE);
    verify(reportMock).report("threatProtection.missingElement", element, element, VALUES);
  }

  @Test
  public void convertXmlPolicyTagWithStructure() {
    XmlPolicyTagMigrationStep step = new XmlPolicyTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement()
        .addContent(getStructureElement());

    step.execute(element, reportMock);

    assertThat(element.getName(), is(XML_CONFIG));
    assertThat(element.getNamespace(), is(XML_THREAT_PROTECTION_NAMESPACE));
    assertThat(element.getAttributes().size(), is(4));
    assertThat(element.getAttribute(NAME).getValue(), is(NAME_VALUE));
    assertThat(element.getAttribute(MAX_NODE_DEPTH).getValue(), is(MAX_NODE_DEPTH_VALUE));
    assertThat(element.getAttribute(MAX_ATTRIBUTE_COUNT_PER_ELEMENT).getValue(), is(MAX_ATTRIBUTE_COUNT_PER_ELEMENT_VALUE));
    assertThat(element.getAttribute(MAX_CHILD_COUNT).getValue(), is(MAX_CHILD_COUNT_VALUE));
    assertThat(element.getContentSize(), is(0));
    verify(reportMock).report("threatProtection.missingElement", element, element, VALUES);
  }

  @Test
  public void convertXmlPolicyTagWithValues() {
    XmlPolicyTagMigrationStep step = new XmlPolicyTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement()
        .addContent(getValuesElement());

    step.execute(element, reportMock);

    assertThat(element.getName(), is(XML_CONFIG));
    assertThat(element.getNamespace(), is(XML_THREAT_PROTECTION_NAMESPACE));
    assertThat(element.getAttributes().size(), is(4));
    assertThat(element.getAttribute(NAME).getValue(), is(NAME_VALUE));
    assertThat(element.getAttribute(MAX_TEXT_LENGTH).getValue(), is(MAX_TEXT_LENGTH_VALUE));
    assertThat(element.getAttribute(MAX_ATTRIBUTE_LENGTH).getValue(), is(MAX_ATTRIBUTE_LENGTH_VALUE));
    assertThat(element.getAttribute(MAX_COMMENT_LENGTH).getValue(), is(MAX_COMMENT_LENGTH_VALUE));
    assertThat(element.getContentSize(), is(0));
    verify(reportMock).report("threatProtection.missingElement", element, element, STRUCTURE);
  }

  private void assertCompleteXmlPolicyTag(Element element) {
    assertThat(element.getName(), is(XML_CONFIG));
    assertThat(element.getNamespace(), is(XML_THREAT_PROTECTION_NAMESPACE));
    assertThat(element.getAttributes().size(), is(7));
    assertThat(element.getAttribute(NAME).getValue(), is(NAME_VALUE));
    assertThat(element.getAttribute(MAX_NODE_DEPTH).getValue(), is(MAX_NODE_DEPTH_VALUE));
    assertThat(element.getAttribute(MAX_ATTRIBUTE_COUNT_PER_ELEMENT).getValue(), is(MAX_ATTRIBUTE_COUNT_PER_ELEMENT_VALUE));
    assertThat(element.getAttribute(MAX_CHILD_COUNT).getValue(), is(MAX_CHILD_COUNT_VALUE));
    assertThat(element.getAttribute(MAX_TEXT_LENGTH).getValue(), is(MAX_TEXT_LENGTH_VALUE));
    assertThat(element.getAttribute(MAX_ATTRIBUTE_LENGTH).getValue(), is(MAX_ATTRIBUTE_LENGTH_VALUE));
    assertThat(element.getAttribute(MAX_COMMENT_LENGTH).getValue(), is(MAX_COMMENT_LENGTH_VALUE));
    assertThat(element.getContentSize(), is(0));
  }

  @Test
  public void convertCompleteXmlPolicyTag() {
    XmlPolicyTagMigrationStep step = new XmlPolicyTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement()
        .addContent(getStructureElement())
        .addContent(getValuesElement());

    step.execute(element, reportMock);

    assertCompleteXmlPolicyTag(element);
  }

  @Test
  public void xmlPomContributionTest() {
    XmlPolicyTagMigrationStep step = new XmlPolicyTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = appModel.getNode(XPATH_NODE_EXPRESSION);

    step.execute(element, reportMock);

    PomModel pm = appModel.getPomModel().get();

    assertThat(pm.getDependencies().size(), is(1));
    Dependency xmlThreatProtectionDependency = pm.getDependencies().get(0);
    assertThat(xmlThreatProtectionDependency.getGroupId(), is(COM_MULESOFT_ANYPOINT_GROUP_ID));
    assertThat(xmlThreatProtectionDependency.getArtifactId(), is(MULE_XML_THREAT_PROTECTION_EXTENSION_ARTIFACT_ID));
    assertThat(xmlThreatProtectionDependency.getVersion(), is(XML_JSON_THREAT_PROTECTION_EXTENSION_VERSION));
    assertThat(xmlThreatProtectionDependency.getClassifier(), is(MULE_PLUGIN_CLASSIFIER));

    assertCompleteXmlPolicyTag(element);
  }

}
