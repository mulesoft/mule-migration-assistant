/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.ee;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
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
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(Parameterized.class)
public class CacheTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path CACHE_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/ee");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "cache-01",
        "cache-02",
        "cache-03",
        "cache-04",
        "cache-05",
        "cache-06"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public CacheTest(String filePrefix) {
    configPath = CACHE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = CACHE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private CacheScope cacheScope;
  private CacheInvalidateKey cacheInvalidateKey;
  private CacheObjectStoreCachingStrategy cacheObjectStoreCachingStrategy;
  private CacheHttpCachingStrategy cacheHttpCachingStrategy;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);

    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(report.getReport(), appModel);

    cacheScope = new CacheScope();
    cacheScope.setExpressionMigrator(expressionMigrator);
    cacheScope.setApplicationModel(appModel);

    cacheInvalidateKey = new CacheInvalidateKey();
    cacheInvalidateKey.setExpressionMigrator(expressionMigrator);
    cacheInvalidateKey.setApplicationModel(appModel);

    cacheObjectStoreCachingStrategy = new CacheObjectStoreCachingStrategy();
    cacheObjectStoreCachingStrategy.setExpressionMigrator(expressionMigrator);
    cacheObjectStoreCachingStrategy.setApplicationModel(appModel);

    cacheHttpCachingStrategy = new CacheHttpCachingStrategy();
    cacheHttpCachingStrategy.setExpressionMigrator(expressionMigrator);
    cacheHttpCachingStrategy.setApplicationModel(appModel);
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, cacheScope.getAppliedTo().getExpression())
        .forEach(node -> cacheScope.execute(node, report.getReport()));
    getElementsFromDocument(doc, cacheInvalidateKey.getAppliedTo().getExpression())
        .forEach(node -> cacheInvalidateKey.execute(node, report.getReport()));
    getElementsFromDocument(doc, cacheObjectStoreCachingStrategy.getAppliedTo().getExpression())
        .forEach(node -> cacheObjectStoreCachingStrategy.execute(node, report.getReport()));
    getElementsFromDocument(doc, cacheHttpCachingStrategy.getAppliedTo().getExpression())
        .forEach(node -> cacheHttpCachingStrategy.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
