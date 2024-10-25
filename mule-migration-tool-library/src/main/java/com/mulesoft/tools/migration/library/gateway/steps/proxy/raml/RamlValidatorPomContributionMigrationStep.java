/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.proxy.raml;

import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

import static com.mulesoft.tools.migration.library.tools.PluginsVersions.targetVersion;

/**
 * Contribute mule-raml-validator and mule-rest-validator plugins to pom.xml
 *
 * @author Mulesoft Inc.
 */
public class RamlValidatorPomContributionMigrationStep implements PomContribution {

  private static final String COM_MULESOFT_ANYPOINT_GROUP_ID = "com.mulesoft.anypoint";
  private static final String MULE_RAML_VALIDATOR_EXTENSION = "mule-raml-validator-extension";
  private static final String MULE_REST_VALIDATOR_EXTENSION = "mule-rest-validator-extension";
  private static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";
  private static final String MULE_REST_VALIDATOR_EXTENSION_VERSION_PROPERTY = "mule-rest-validator-extension";
  private static final String ARBITRARY_MULE_RAML_VALIDATOR_EXTENSION_VERSION = "${raml.validator.extension.version}";

  @Override
  public String getDescription() {
    return "Pom contribution migration step for HTTP transform element";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport migrationReport) throws RuntimeException {
    pomModel.addDependency(new Dependency.DependencyBuilder()
        .withGroupId(COM_MULESOFT_ANYPOINT_GROUP_ID)
        .withArtifactId(MULE_REST_VALIDATOR_EXTENSION)
        .withVersion(targetVersion(MULE_REST_VALIDATOR_EXTENSION_VERSION_PROPERTY))
        .withClassifier(MULE_PLUGIN_CLASSIFIER)
        .build());
    pomModel.removeDependency(new Dependency.DependencyBuilder()
        .withGroupId(COM_MULESOFT_ANYPOINT_GROUP_ID)
        .withArtifactId(MULE_RAML_VALIDATOR_EXTENSION)
        .withVersion(ARBITRARY_MULE_RAML_VALIDATOR_EXTENSION_VERSION)
        .withClassifier(MULE_PLUGIN_CLASSIFIER)
        .build());
  }
}
