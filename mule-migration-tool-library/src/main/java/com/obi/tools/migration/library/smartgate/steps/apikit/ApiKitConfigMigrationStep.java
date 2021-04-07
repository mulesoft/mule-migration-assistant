/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.steps.apikit;

import com.mulesoft.tools.migration.library.gateway.steps.GatewayMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;

import java.util.Optional;

/**
 * Migrate api tag
 *
 * @author Mulesoft Inc.
 */
public class ApiKitConfigMigrationStep extends GatewayMigrationStep {


  private static final String OBI_SMARTGATE_ANYPOINT_EXCHANGE_ASSET_BUSINESSGROUPID =
      "obi.smartgate.anypoint.exchange.asset.businessgroupid";
  private static final String OBI_SMARTGATE_ANYPOINT_EXCHANGE_ASSET_VERSION = "obi.smartgate.anypoint.exchange.asset.version";
  private static final String RAML = "raml";
  private static final String CONFIG_TAG_NAME = "config";
  private static final String API = "api";

  public ApiKitConfigMigrationStep() {
    super(GatewayNamespaces.APIKIT_NAMESPACE, CONFIG_TAG_NAME);
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {

    final Attribute attribute = element.getAttribute(RAML);
    String raml = null;
    if (attribute != null) {
      raml = attribute.getValue();
      element.removeAttribute(attribute);

    }
    final Optional<PomModel> optionalPomModel = getApplicationModel().getPomModel();
    if (optionalPomModel.isPresent() && raml != null) {

      // Create Attribute api="resource::a4ffec2c-4120-4a26-b44f-c8def51ddf89:showcase-papi:1.0.2:raml:zip:showcase-papi.raml"
      final PomModel pomModel = optionalPomModel.get();
      final String artifactId = pomModel.getArtifactId();
      final String businesssGroup = pomModel.getProperties().getProperty(OBI_SMARTGATE_ANYPOINT_EXCHANGE_ASSET_BUSINESSGROUPID);
      final String version = pomModel.getProperties().getProperty(OBI_SMARTGATE_ANYPOINT_EXCHANGE_ASSET_VERSION);
      final String apiValue = "resource::" + businesssGroup + ":" + artifactId + ":" + version + ":raml:zip:" + raml;
      element.setAttribute(new Attribute(API, apiValue));
    }

  }
}
