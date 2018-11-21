/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.os;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(Parameterized.class)
public class ObjectStoreMigrationTest {

  private static final Path EE_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/os");

  @Rule
  public ReportVerification report = new ReportVerification();

  @Parameterized.Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "object-store-01",
        "object-store-02",
        "object-store-03",
        "object-store-04",
        "object-store-05",
        "object-store-06",
        "object-store-07",
        "object-store-08"
    };
  }

  private final Path configPath;
  private final Path targetPath;
  private Document doc;
  private ApplicationModel appModel;

  public ObjectStoreMigrationTest(String filePrefix) {
    configPath = EE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = EE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private OSBasicOperations osBasicOperations;
  private OSConfig osConfig;
  private OSDisposeStore osDisposeStore;
  private OSDualStore osDualStore;
  private OSRetrieveStore osRetrieveStore;
  private OSRetrieve osRetrieve;
  private OSStore osStore;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    MelToDwExpressionMigrator expressionMigrator =
        new MelToDwExpressionMigrator(report.getReport(), mock(ApplicationModel.class));

    appModel = mock(ApplicationModel.class);
    when(appModel.getNodes(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]));

    when(appModel.getNode(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]).stream().findFirst()
            .orElse(null));

    osBasicOperations = new OSBasicOperations();
    osBasicOperations.setApplicationModel(appModel);
    osConfig = new OSConfig();
    osConfig.setApplicationModel(appModel);
    osDisposeStore = new OSDisposeStore();
    osDisposeStore.setApplicationModel(appModel);
    osDualStore = new OSDualStore();
    osDualStore.setApplicationModel(appModel);
    osRetrieveStore = new OSRetrieveStore();
    osRetrieveStore.setApplicationModel(appModel);
    osRetrieveStore.setExpressionMigrator(expressionMigrator);
    osStore = new OSStore();
    osStore.setApplicationModel(appModel);
    osStore.setExpressionMigrator(expressionMigrator);
    osRetrieve = new OSRetrieve();
    osRetrieve.setApplicationModel(appModel);
    osRetrieve.setExpressionMigrator(expressionMigrator);
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, osBasicOperations.getAppliedTo().getExpression())
        .forEach(node -> osBasicOperations.execute(node, report.getReport()));
    getElementsFromDocument(doc, osConfig.getAppliedTo().getExpression())
        .forEach(node -> osConfig.execute(node, report.getReport()));
    getElementsFromDocument(doc, osDisposeStore.getAppliedTo().getExpression())
        .forEach(node -> osDisposeStore.execute(node, report.getReport()));
    getElementsFromDocument(doc, osDualStore.getAppliedTo().getExpression())
        .forEach(node -> osDualStore.execute(node, report.getReport()));
    getElementsFromDocument(doc, osRetrieveStore.getAppliedTo().getExpression())
        .forEach(node -> osRetrieveStore.execute(node, report.getReport()));
    getElementsFromDocument(doc, osStore.getAppliedTo().getExpression())
        .forEach(node -> osStore.execute(node, report.getReport()));
    getElementsFromDocument(doc, osRetrieve.getAppliedTo().getExpression())
        .forEach(node -> osRetrieve.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
