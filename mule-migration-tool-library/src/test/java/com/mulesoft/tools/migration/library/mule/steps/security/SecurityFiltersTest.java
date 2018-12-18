/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.security;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.security.crc32.Crc32Calculate;
import com.mulesoft.tools.migration.library.mule.steps.security.crc32.Crc32Config;
import com.mulesoft.tools.migration.library.mule.steps.security.crc32.Crc32Filter;
import com.mulesoft.tools.migration.library.mule.steps.security.filter.ByIpRangeCidrFilter;
import com.mulesoft.tools.migration.library.mule.steps.security.filter.ByIpRangeFilter;
import com.mulesoft.tools.migration.library.mule.steps.security.filter.ByIpRegexFilter;
import com.mulesoft.tools.migration.library.mule.steps.security.filter.ExpiredFilter;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.tck.ReportVerification;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RunWith(Parameterized.class)
public class SecurityFiltersTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path ENCRYPTION_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/security");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "crc32-01",
        "crc32-02",
        "filters-01",
        "filters-01b",
        "filters-02",
        "filters-03",
        "filters-04",
        "filters-05"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public SecurityFiltersTest(String filePrefix) {
    configPath = ENCRYPTION_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = ENCRYPTION_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private Crc32Filter crc32Filter;
  private Crc32Calculate crc32Calculate;
  private Crc32Config crc32Config;
  private ExpiredFilter expiredFilter;
  private ByIpRegexFilter byIpRegexFilter;
  private ByIpRangeFilter byIpRangeFilter;
  private ByIpRangeCidrFilter byIpRangeCidrFilter;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    appModel = mock(ApplicationModel.class);
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());

    MelToDwExpressionMigrator expressionMigrator =
        new MelToDwExpressionMigrator(report.getReport(), mock(ApplicationModel.class));

    appModel = mock(ApplicationModel.class);
    when(appModel.getNodes(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]));
    when(appModel.getNode(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]).iterator().next());
    when(appModel.getNodeOptional(any(String.class)))
        .thenAnswer(invocation -> {
          List<Element> elementsFromDocument = getElementsFromDocument(doc, (String) invocation.getArguments()[0]);
          if (elementsFromDocument.isEmpty()) {
            return empty();
          } else {
            return of(elementsFromDocument.iterator().next());
          }
        });
    when(appModel.getProjectBasePath()).thenReturn(temp.newFolder().toPath());
    when(appModel.getPomModel()).thenReturn(of(mock(PomModel.class)));

    crc32Filter = new Crc32Filter();
    crc32Filter.setExpressionMigrator(expressionMigrator);
    crc32Filter.setApplicationModel(appModel);

    crc32Calculate = new Crc32Calculate();
    crc32Calculate.setExpressionMigrator(expressionMigrator);
    crc32Calculate.setApplicationModel(appModel);

    crc32Config = new Crc32Config();

    expiredFilter = new ExpiredFilter();
    expiredFilter.setExpressionMigrator(expressionMigrator);
    expiredFilter.setApplicationModel(appModel);

    byIpRegexFilter = new ByIpRegexFilter();
    byIpRegexFilter.setExpressionMigrator(expressionMigrator);
    byIpRegexFilter.setApplicationModel(appModel);

    byIpRangeFilter = new ByIpRangeFilter();
    byIpRangeFilter.setExpressionMigrator(expressionMigrator);
    byIpRangeFilter.setApplicationModel(appModel);

    byIpRangeCidrFilter = new ByIpRangeCidrFilter();
    byIpRangeCidrFilter.setExpressionMigrator(expressionMigrator);
    byIpRangeCidrFilter.setApplicationModel(appModel);

  }

  public void migrate(AbstractApplicationModelMigrationStep migrationStep) {
    getElementsFromDocument(doc, migrationStep.getAppliedTo().getExpression())
        .forEach(node -> migrationStep.execute(node, report.getReport()));
  }

  @Test
  public void execute() throws Exception {
    migrate(crc32Filter);
    migrate(crc32Calculate);
    migrate(crc32Config);
    migrate(expiredFilter);
    migrate(byIpRegexFilter);
    migrate(byIpRangeFilter);
    migrate(byIpRangeCidrFilter);

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
