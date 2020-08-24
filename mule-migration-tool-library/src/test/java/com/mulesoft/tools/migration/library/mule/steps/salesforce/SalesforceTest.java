/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.salesforce;

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

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

@RunWith(Parameterized.class)
public class SalesforceTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path SALESFORCE_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/salesforce");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "salesforce-create",
        "salesforce-createWithoutHeaders",
        "salesforce-createWithAccessTokenId",
        "salesforce-createWithCreateObjectsManually",
        "salesforce-createWithEditInlineHeaders",
        "salesforce-basicAuthentication",
        "salesforce-basicAuth",
        "salesforce-update",
        "salesforce-updateManuallyObjectsAndHeaders",
        "salesforce-updateWithAccessTokenId",
        "salesforce-upsert",
        "salesforce-upsertWithAccessTokenId",
        "salesforce-upsertWithoutHeaders",
        "salesforce-upsertWithCreateObjectsManually",
        "salesforce-upsertWithEditInlineHeaders",
        "salesforce-upsertWithoutExternalIdFieldName",
        "salesforce-upsertBulk",
        "salesforce-upsertBulkWithAccessTokenId",
        "salesforce-upsertBulkWithoutHeaders",
        "salesforce-upsertBulkWithCreateObjectsManually",
        "salesforce-upsertBulkWithEditInlineHeaders",
        "salesforce-upsertBulkWithoutExternalIdFieldName",
        "salesforce-retrieveWithIdsAndFieldsAddedManually",
        "salesforce-retrieveWithIdsAndFieldsFromExpression",
        "salesforce-retrieveWithIdsAddedManuallyAndFieldsFromExpression",
        "salesforce-retrieveWithEditInlineHeaders",
        "salesforce-retrieveWithAccessTokenId",
        "salesforce-retrieveWithoutIds",
        "salesforce-queryDsqlDefaultFetchSize",
        "salesforce-queryNativeNotDefaultFetchSize",
        "salesforce-queryWithAccessTokenId",
        "salesforce-queryWithEditInlineHeadersNotDefaultFetchSize",
        "salesforce-queryWithEditInlineHeadersDefaultFetchSize",
        "salesforce-queryWithoutHeadersNotDefaultFetchSize",
        "salesforce-queryWithoutHeadersDefaultFetchSize",
        "salesforce-querySingleDsql",
        "salesforce-querySingleNative",
        "salesforce-querySingleWithAccessTokenId",
        "salesforce-querySingleWithEditInlineHeaders",
        "salesforce-querySingleWithoutHeaders",
        "salesforce-queryAllDsqlDefaultFetchSize",
        "salesforce-queryAllNativeNotDefaultFetchSize",
        "salesforce-queryAllWithAccessTokenId",
        "salesforce-queryAllWithEditInlineHeadersNotDefaultFetchSize",
        "salesforce-queryAllWithEditInlineHeadersDefaultFetchSize",
        "salesforce-queryAllWithoutHeadersNotDefaultFetchSize",
        "salesforce-queryAllWithoutHeadersDefaultFetchSize",
        "salesforce-createJob",
        "salesforce-createJobWithoutRequest",
        "salesforce-createJobWithAccessTokenId",
        "salesforce-createJobWithConcurrencyMode",
        "salesforce-createJobWithConcurrencyModeAndContentType",
        "salesforce-createJobWithEditInlineHeaders",
        "salesforce-oauthUsernamePassword",
        "salesforce-oauthUsernamePasswordWithApexClassesAddedManually",
        "salesforce-oauthJwtBearer",
        "salesforce-oauthJwtBearerWithApexClassesAddedManually",
        "salesforce-oauthJwtBearerWithReconnectForever",
        "salesforce-invokeApexRestMethod",
        "salesforce-invokeApexRestMethodWithQueryParamsAddedManually",
        "salesforce-invokeApexRestMethodWithAccessTokenId",
        "salesforce-invokeApexRestMethodWithoutRequest",
        "salesforce-replayStreamingChannel",
        "salesforce-subscribeTopic",
        "salesforce-subscribeStreamingChannel",
        "salesforce-nonPaginatedQueryDsql",
        "salesforce-nonPaginatedQueryNative",
        "salesforce-nonPaginatedQueryWithAccessTokenId",
        "salesforce-nonPaginatedQueryWithEditInlineHeaders",
        "salesforce-nonPaginatedQueryWithoutHeaders"
    };
  }

  private final Path configPath;
  private final Path targetPath;
  private Document doc;
  private ApplicationModel appModel;
  private CreateOperation createOperation;
  private UpsertOperation upsertOperation;
  private UpsertBulkOperation upsertBulkOperation;
  private RetrieveOperation retrieveOperation;
  private UpdateOperation updateOperation;
  private QueryOperation queryOperation;
  private QuerySingleOperation querySingleOperation;
  private QueryAllOperation queryAllOperation;
  private CachedBasicConfiguration cachedBasicConfiguration;
  private CreateJobOperation createJobOperation;
  private OauthUsernamePasswordConfiguration oauthUsernamePasswordConfiguration;
  private OAuthJwtBearerConfiguration oAuthJwtBearerConfiguration;
  private InvokeApexRestMethodOperation invokeApexRestMethodOperation;
  private ReplayStreamingChannelSource replayStreamingChannelSource;
  private SubscribeTopicSource subscribeTopicSource;
  private SubscribeStreamingChannelSource subscribeStreamingChannelSource;
  private NonPaginatedQueryOperation nonPaginatedQueryOperation;

  public SalesforceTest(String filePrefix) {
    this.configPath = SALESFORCE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    this.targetPath = SALESFORCE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);

    createOperation = new CreateOperation();
    upsertOperation = new UpsertOperation();
    upsertBulkOperation = new UpsertBulkOperation();
    retrieveOperation = new RetrieveOperation();
    updateOperation = new UpdateOperation();
    queryOperation = new QueryOperation();
    querySingleOperation = new QuerySingleOperation();
    queryAllOperation = new QueryAllOperation();
    cachedBasicConfiguration = new CachedBasicConfiguration();
    createJobOperation = new CreateJobOperation();
    oauthUsernamePasswordConfiguration = new OauthUsernamePasswordConfiguration();
    oAuthJwtBearerConfiguration = new OAuthJwtBearerConfiguration();
    invokeApexRestMethodOperation = new InvokeApexRestMethodOperation();
    replayStreamingChannelSource = new ReplayStreamingChannelSource();
    subscribeTopicSource = new SubscribeTopicSource();
    subscribeStreamingChannelSource = new SubscribeStreamingChannelSource();
    nonPaginatedQueryOperation = new NonPaginatedQueryOperation();

    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(report.getReport(), appModel);
    createOperation.setExpressionMigrator(expressionMigrator);
    upsertOperation.setExpressionMigrator(expressionMigrator);
    upsertBulkOperation.setExpressionMigrator(expressionMigrator);
    retrieveOperation.setExpressionMigrator(expressionMigrator);
    updateOperation.setExpressionMigrator(expressionMigrator);
    queryOperation.setExpressionMigrator(expressionMigrator);
    querySingleOperation.setExpressionMigrator(expressionMigrator);
    queryAllOperation.setExpressionMigrator(expressionMigrator);
    cachedBasicConfiguration.setExpressionMigrator(expressionMigrator);
    createJobOperation.setExpressionMigrator(expressionMigrator);
    oauthUsernamePasswordConfiguration.setExpressionMigrator(expressionMigrator);
    oAuthJwtBearerConfiguration.setExpressionMigrator(expressionMigrator);
    invokeApexRestMethodOperation.setExpressionMigrator(expressionMigrator);
    replayStreamingChannelSource.setExpressionMigrator(expressionMigrator);
    subscribeTopicSource.setExpressionMigrator(expressionMigrator);
    subscribeStreamingChannelSource.setExpressionMigrator(expressionMigrator);
    nonPaginatedQueryOperation.setExpressionMigrator(expressionMigrator);
  }

  public void migrate(AbstractApplicationModelMigrationStep migrationStep) {
    getElementsFromDocument(doc, migrationStep.getAppliedTo().getExpression())
        .forEach(node -> migrationStep.execute(node, report.getReport()));
  }

  @Test
  public void execute() throws Exception {
    migrate(createOperation);
    migrate(upsertOperation);
    migrate(upsertBulkOperation);
    migrate(retrieveOperation);
    migrate(updateOperation);
    migrate(queryOperation);
    migrate(querySingleOperation);
    migrate(queryAllOperation);
    migrate(cachedBasicConfiguration);
    migrate(createJobOperation);
    migrate(oauthUsernamePasswordConfiguration);
    migrate(oAuthJwtBearerConfiguration);
    migrate(invokeApexRestMethodOperation);
    migrate(replayStreamingChannelSource);
    migrate(subscribeTopicSource);
    migrate(subscribeStreamingChannelSource);
    migrate(nonPaginatedQueryOperation);

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    if (doc.getBaseURI().contains("AccessTokenId")) {
      report.expectReportEntry("salesforce.accessTokenId");
    }
    if (doc.getBaseURI().contains("FetchSize")) {
      report.expectReportEntry("salesforce.fetchSize");
    }

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
