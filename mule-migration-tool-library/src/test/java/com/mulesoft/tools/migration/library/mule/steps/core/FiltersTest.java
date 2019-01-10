/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.core.filter.AndFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.CustomFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.ExceptionTypeFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.ExpressionFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.FilterReference;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.IdempotentMessageFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.IdempotentSecureHashMessageFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.MessageFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.MessageFilterReference;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.MessagePropertyFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.NotFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.OrFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.PayloadTypeFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.RegexFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.WildcardFilter;
import com.mulesoft.tools.migration.library.mule.steps.scripting.ScriptingFilterMigration;
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
public class FiltersTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

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
        "filter-14",
        "filter-15",
        "filter-16",
        "filter-17"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public FiltersTest(String filterPrefix) {
    configPath = FILTERS_CONFIG_EXAMPLES_PATH.resolve(filterPrefix + "-original.xml");
    targetPath = FILTERS_CONFIG_EXAMPLES_PATH.resolve(filterPrefix + ".xml");
  }

  private ScriptingFilterMigration scriptingFilterMigration;
  private FilterReference filterReference;
  private MessageFilterReference messageFilterReference;
  private MessageFilter messageFilter;
  private ExpressionFilter expressionFilter;
  private RegexFilter regexFilter;
  private WildcardFilter wildcardFilter;
  private PayloadTypeFilter payloadTypeFilter;
  private ExceptionTypeFilter exceptionTypeFilter;
  private MessagePropertyFilter messagePropertyFilter;
  private CustomFilter customFilter;
  private IdempotentMessageFilter idempotentMsgFilter;
  private IdempotentSecureHashMessageFilter idempotentSecureHashMsgFilter;

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
    appModel = mockApplicationModel(doc, temp);
    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(report.getReport(), appModel);

    customFilter = new CustomFilter();
    scriptingFilterMigration = new ScriptingFilterMigration();
    scriptingFilterMigration.setApplicationModel(appModel);
    filterReference = new FilterReference();
    filterReference.setExpressionMigrator(expressionMigrator);
    filterReference.setApplicationModel(appModel);
    messageFilterReference = new MessageFilterReference();
    messageFilterReference.setExpressionMigrator(expressionMigrator);
    messageFilterReference.setApplicationModel(appModel);
    messageFilter = new MessageFilter();
    messageFilter.setExpressionMigrator(expressionMigrator);
    messageFilter.setApplicationModel(appModel);
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
    exceptionTypeFilter = new ExceptionTypeFilter();
    exceptionTypeFilter.setExpressionMigrator(expressionMigrator);
    exceptionTypeFilter.setApplicationModel(appModel);
    messagePropertyFilter = new MessagePropertyFilter();
    messagePropertyFilter.setExpressionMigrator(expressionMigrator);
    messagePropertyFilter.setApplicationModel(appModel);
    customFilter = new CustomFilter();
    customFilter.setExpressionMigrator(expressionMigrator);
    customFilter.setApplicationModel(appModel);
    idempotentMsgFilter = new IdempotentMessageFilter();
    idempotentMsgFilter.setExpressionMigrator(expressionMigrator);
    idempotentMsgFilter.setApplicationModel(appModel);
    idempotentSecureHashMsgFilter = new IdempotentSecureHashMessageFilter();
    idempotentSecureHashMsgFilter.setExpressionMigrator(expressionMigrator);
    idempotentSecureHashMsgFilter.setApplicationModel(appModel);
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
    getElementsFromDocument(doc, scriptingFilterMigration.getAppliedTo().getExpression())
        .forEach(node -> scriptingFilterMigration.execute(node, report.getReport()));

    getElementsFromDocument(doc, filterReference.getAppliedTo().getExpression())
        .forEach(node -> filterReference.execute(node, report.getReport()));
    getElementsFromDocument(doc, messageFilterReference.getAppliedTo().getExpression())
        .forEach(node -> messageFilterReference.execute(node, report.getReport()));
    getElementsFromDocument(doc, messageFilter.getAppliedTo().getExpression())
        .forEach(node -> messageFilter.execute(node, report.getReport()));
    getElementsFromDocument(doc, customFilter.getAppliedTo().getExpression())
        .forEach(node -> customFilter.execute(node, report.getReport()));
    getElementsFromDocument(doc, expressionFilter.getAppliedTo().getExpression())
        .forEach(node -> expressionFilter.execute(node, report.getReport()));
    getElementsFromDocument(doc, regexFilter.getAppliedTo().getExpression())
        .forEach(node -> regexFilter.execute(node, report.getReport()));
    getElementsFromDocument(doc, wildcardFilter.getAppliedTo().getExpression())
        .forEach(node -> wildcardFilter.execute(node, report.getReport()));
    getElementsFromDocument(doc, payloadTypeFilter.getAppliedTo().getExpression())
        .forEach(node -> payloadTypeFilter.execute(node, report.getReport()));
    getElementsFromDocument(doc, exceptionTypeFilter.getAppliedTo().getExpression())
        .forEach(node -> exceptionTypeFilter.execute(node, report.getReport()));
    getElementsFromDocument(doc, messagePropertyFilter.getAppliedTo().getExpression())
        .forEach(node -> messagePropertyFilter.execute(node, report.getReport()));

    getElementsFromDocument(doc, andFilter.getAppliedTo().getExpression())
        .forEach(node -> andFilter.execute(node, report.getReport()));
    getElementsFromDocument(doc, orFilter.getAppliedTo().getExpression())
        .forEach(node -> orFilter.execute(node, report.getReport()));
    getElementsFromDocument(doc, notFilter.getAppliedTo().getExpression())
        .forEach(node -> notFilter.execute(node, report.getReport()));

    getElementsFromDocument(doc, idempotentMsgFilter.getAppliedTo().getExpression())
        .forEach(node -> idempotentMsgFilter.execute(node, report.getReport()));
    getElementsFromDocument(doc, idempotentSecureHashMsgFilter.getAppliedTo().getExpression())
        .forEach(node -> idempotentSecureHashMsgFilter.execute(node, report.getReport()));

    getElementsFromDocument(doc, globalElementsCleanup.getAppliedTo().getExpression())
        .forEach(node -> globalElementsCleanup.execute(node, report.getReport()));
    getElementsFromDocument(doc, removeSyntheticMigrationAttributes.getAppliedTo().getExpression())
        .forEach(node -> removeSyntheticMigrationAttributes.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
