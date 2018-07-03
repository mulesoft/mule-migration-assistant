/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.ee;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

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
  private final MigrationReport reportMock;

  public CacheTest(String filePrefix) {
    configPath = CACHE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = CACHE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
    reportMock = mock(MigrationReport.class);
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
    appModel = mock(ApplicationModel.class);
    when(appModel.getNodes(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]));
    when(appModel.getNode(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]).iterator().next());
    when(appModel.getNodeOptional(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]).stream().findAny());
    when(appModel.getProjectBasePath()).thenReturn(temp.newFolder().toPath());
    when(appModel.getPomModel()).thenReturn(empty());

    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(reportMock, mock(ApplicationModel.class));

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
        .forEach(node -> cacheScope.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, cacheInvalidateKey.getAppliedTo().getExpression())
        .forEach(node -> cacheInvalidateKey.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, cacheObjectStoreCachingStrategy.getAppliedTo().getExpression())
        .forEach(node -> cacheObjectStoreCachingStrategy.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, cacheHttpCachingStrategy.getAppliedTo().getExpression())
        .forEach(node -> cacheHttpCachingStrategy.execute(node, mock(MigrationReport.class)));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
