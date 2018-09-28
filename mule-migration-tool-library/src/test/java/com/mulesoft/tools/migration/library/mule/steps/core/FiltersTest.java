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
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.core.filter.AndFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.CustomFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.ExpressionFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.FilterReference;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.NotFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.OrFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.PayloadTypeFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.RegexFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.WildcardFilter;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

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
public class FiltersTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private static final Path FILTERS_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/filters");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "filter-01",
        "filter-02",
        "filter-03",
        "filter-04",
        "filter-05",
        "filter-06",
        "filter-07",
        "filter-08",
        "filter-09",
        "filter-10",
        "filter-11",
        "filter-12",
        "filter-13",
        "filter-14"
    };
  }

  private final Path configPath;
  private final Path targetPath;
  private final MigrationReport reportMock;

  public FiltersTest(String filterPrefix) {
    configPath = FILTERS_CONFIG_EXAMPLES_PATH.resolve(filterPrefix + "-original.xml");
    targetPath = FILTERS_CONFIG_EXAMPLES_PATH.resolve(filterPrefix + ".xml");
    reportMock = mock(MigrationReport.class);
  }

  private FilterReference filterReference;
  private ExpressionFilter expressionFilter;
  private RegexFilter regexFilter;
  private WildcardFilter wildcardFilter;
  private PayloadTypeFilter payloadTypeFilter;
  private CustomFilter customFilter;

  private AndFilter andFilter;
  private OrFilter orFilter;
  private NotFilter notFilter;

  private RemoveSyntheticMigrationAttributes removeSyntheticMigrationAttributes;
  private GlobalElementsCleanup globalElementsCleanup;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());

    customFilter = new CustomFilter();

    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(reportMock, mock(ApplicationModel.class));
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

    filterReference = new FilterReference();
    filterReference.setExpressionMigrator(expressionMigrator);
    filterReference.setApplicationModel(appModel);
    expressionFilter = new ExpressionFilter();
    expressionFilter.setExpressionMigrator(expressionMigrator);
    expressionFilter.setApplicationModel(appModel);
    regexFilter = new RegexFilter();
    regexFilter.setExpressionMigrator(expressionMigrator);
    regexFilter.setApplicationModel(appModel);
    wildcardFilter = new WildcardFilter();
    wildcardFilter.setExpressionMigrator(expressionMigrator);
    wildcardFilter.setApplicationModel(appModel);
    payloadTypeFilter = new PayloadTypeFilter();
    payloadTypeFilter.setExpressionMigrator(expressionMigrator);
    payloadTypeFilter.setApplicationModel(appModel);
    customFilter = new CustomFilter();
    customFilter.setExpressionMigrator(expressionMigrator);
    customFilter.setApplicationModel(appModel);
    andFilter = new AndFilter();
    andFilter.setExpressionMigrator(expressionMigrator);
    andFilter.setApplicationModel(appModel);
    orFilter = new OrFilter();
    orFilter.setExpressionMigrator(expressionMigrator);
    orFilter.setApplicationModel(appModel);
    notFilter = new NotFilter();
    notFilter.setExpressionMigrator(expressionMigrator);
    notFilter.setApplicationModel(appModel);
    globalElementsCleanup = new GlobalElementsCleanup();
    removeSyntheticMigrationAttributes = new RemoveSyntheticMigrationAttributes();
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, filterReference.getAppliedTo().getExpression())
        .forEach(node -> filterReference.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, customFilter.getAppliedTo().getExpression())
        .forEach(node -> customFilter.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, expressionFilter.getAppliedTo().getExpression())
        .forEach(node -> expressionFilter.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, regexFilter.getAppliedTo().getExpression())
        .forEach(node -> regexFilter.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, wildcardFilter.getAppliedTo().getExpression())
        .forEach(node -> wildcardFilter.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, payloadTypeFilter.getAppliedTo().getExpression())
        .forEach(node -> payloadTypeFilter.execute(node, mock(MigrationReport.class)));

    getElementsFromDocument(doc, andFilter.getAppliedTo().getExpression())
        .forEach(node -> andFilter.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, orFilter.getAppliedTo().getExpression())
        .forEach(node -> orFilter.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, notFilter.getAppliedTo().getExpression())
        .forEach(node -> notFilter.execute(node, mock(MigrationReport.class)));

    getElementsFromDocument(doc, globalElementsCleanup.getAppliedTo().getExpression())
        .forEach(node -> globalElementsCleanup.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, removeSyntheticMigrationAttributes.getAppliedTo().getExpression())
        .forEach(node -> removeSyntheticMigrationAttributes.execute(node, mock(MigrationReport.class)));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
