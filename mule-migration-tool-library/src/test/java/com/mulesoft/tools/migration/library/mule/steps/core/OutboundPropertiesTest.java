/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(Parameterized.class)
public class OutboundPropertiesTest {

  private static final Path CORE_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/core");

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "outbound-properties-01",
        "outbound-properties-02",
        // TODO Migrate spring beans
        // "outbound-properties-03",
        "outbound-properties-04",
        "outbound-properties-05"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public OutboundPropertiesTest(String filePrefix) {
    configPath = CORE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = CORE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private SetProperty setProperty;
  private CopyProperties copyProperties;
  private MessagePropertiesTransformer mpt;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    setProperty = new SetProperty();
    copyProperties = new CopyProperties();
    mpt = new MessagePropertiesTransformer();
    mpt.setExpressionMigrator(new MelToDwExpressionMigrator(mock(MigrationReport.class)));
  }

  @Ignore
  @Test(expected = MigrationStepException.class)
  public void executeWithNullElement() throws Exception {
    setProperty.execute(null, mock(MigrationReport.class));
  }

  @Test
  public void execute() throws Exception {
    Document doc =
        getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    getElementsFromDocument(doc, setProperty.getAppliedTo().getExpression())
        .forEach(node -> setProperty.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, copyProperties.getAppliedTo().getExpression())
        .forEach(node -> copyProperties.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, mpt.getAppliedTo().getExpression())
        .forEach(node -> mpt.execute(node, mock(MigrationReport.class)));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
