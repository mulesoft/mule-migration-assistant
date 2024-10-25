/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.security;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
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
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.tck.ReportVerification;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
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
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);

    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(report.getReport(), appModel);

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
