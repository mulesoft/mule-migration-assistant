/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.munit.steps;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.mulesoft.tools.migration.tck.ReportVerification;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class MUnitConfigTest {

  private static final String MUNIT_SAMPLE_XML = "munit-config.xml";
  private static final Path MUNIT_EXAMPLES_PATH = Paths.get("munit/examples");
  private static final Path MUNIT_SAMPLE_PATH = MUNIT_EXAMPLES_PATH.resolve(MUNIT_SAMPLE_XML);

  @Rule
  public ReportVerification report = new ReportVerification();

  private MUnitConfig munitConfig;
  private Element node;
  private Document doc;

  @Before
  public void setUp() throws Exception {
    munitConfig = new MUnitConfig();
    doc = getDocument(this.getClass().getClassLoader().getResource(MUNIT_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, munitConfig.getAppliedTo().getExpression()).get(0);
    munitConfig.execute(node, report.getReport());
  }

  @Test
  public void migrateConfigName() throws Exception {
    assertThat("The munit:config name attribute should be equals to the test-suite file name",
               node.getAttribute("name").getValue(),
               equalTo(FilenameUtils.getBaseName(MUNIT_SAMPLE_XML)));
  }

  @Test
  public void removeMockConnectionsAttribute() throws Exception {
    assertThat("The mock-connectors attribute is present on munit config",
               node.getAttribute("mock-connectors"),
               equalTo(null));
  }

  @Test
  public void removeMockInboundsAttribute() throws Exception {
    assertThat("The mock-inbounds attribute is present on munit config",
               node.getAttribute("mock-inbounds"),
               equalTo(null));
  }

}
