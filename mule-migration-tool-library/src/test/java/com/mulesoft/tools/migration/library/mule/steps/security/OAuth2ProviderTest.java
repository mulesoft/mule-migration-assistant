/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.security;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.http.HttpConfig;
import com.mulesoft.tools.migration.library.mule.steps.security.oauth2.OAuth2ProviderConfig;
import com.mulesoft.tools.migration.library.mule.steps.security.oauth2.OAuth2ProviderCreateClient;
import com.mulesoft.tools.migration.library.mule.steps.security.oauth2.OAuth2ProviderDeleteClient;
import com.mulesoft.tools.migration.library.mule.steps.security.oauth2.OAuth2ProviderRevokeToken;
import com.mulesoft.tools.migration.library.mule.steps.security.oauth2.OAuth2ProviderStoresConfigRemove;
import com.mulesoft.tools.migration.library.mule.steps.security.oauth2.OAuth2ProviderValidate;
import com.mulesoft.tools.migration.library.mule.steps.security.oauth2.OAuth2ProviderValidateClient;
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
public class OAuth2ProviderTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path OAUTH2_PROVIDER_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/security/oauth2");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "oauth2-provider-01",
        "oauth2-provider-02",
        "oauth2-provider-03",
        "oauth2-provider-04",
        "oauth2-provider-05",
        "oauth2-provider-06",
        "oauth2-provider-07",
        "oauth2-provider-08",
        "oauth2-provider-09",
        "oauth2-provider-10",
        "oauth2-provider-11",
        "oauth2-provider-12",
        "oauth2-provider-13",
        "oauth2-provider-14",
        "oauth2-provider-15",
        "oauth2-provider-16"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public OAuth2ProviderTest(String filePrefix) {
    configPath = OAUTH2_PROVIDER_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = OAUTH2_PROVIDER_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private Document doc;
  private ApplicationModel appModel;

  private OAuth2ProviderConfig config;
  private OAuth2ProviderValidate validate;
  private OAuth2ProviderRevokeToken revoke;
  private OAuth2ProviderValidateClient validateClient;
  private OAuth2ProviderCreateClient createClient;
  private OAuth2ProviderDeleteClient deleteClient;
  private OAuth2ProviderStoresConfigRemove storesConfigRemove;
  private HttpConfig httpConfig;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);

    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(report.getReport(), appModel);

    config = new OAuth2ProviderConfig();
    config.setApplicationModel(appModel);

    validate = new OAuth2ProviderValidate();
    validate.setApplicationModel(appModel);

    revoke = new OAuth2ProviderRevokeToken();
    revoke.setExpressionMigrator(expressionMigrator);
    revoke.setApplicationModel(appModel);

    validateClient = new OAuth2ProviderValidateClient();
    validateClient.setApplicationModel(appModel);

    createClient = new OAuth2ProviderCreateClient();
    createClient.setExpressionMigrator(expressionMigrator);
    createClient.setApplicationModel(appModel);

    deleteClient = new OAuth2ProviderDeleteClient();
    deleteClient.setExpressionMigrator(expressionMigrator);
    deleteClient.setApplicationModel(appModel);

    storesConfigRemove = new OAuth2ProviderStoresConfigRemove();
    storesConfigRemove.setApplicationModel(appModel);

    httpConfig = new HttpConfig();
    httpConfig.setApplicationModel(appModel);
  }

  public void migrate(AbstractApplicationModelMigrationStep migrationStep) {
    getElementsFromDocument(doc, migrationStep.getAppliedTo().getExpression())
        .forEach(node -> migrationStep.execute(node, report.getReport()));
  }

  @Test
  public void execute() throws Exception {
    migrate(config);
    migrate(validate);
    migrate(revoke);
    migrate(validateClient);
    migrate(createClient);
    migrate(deleteClient);
    migrate(storesConfigRemove);
    migrate(httpConfig);

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
