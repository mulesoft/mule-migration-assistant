/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.threatprotection;

import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

import static com.mulesoft.tools.migration.library.tools.PluginsVersions.targetVersion;

/**
 * Contribute mule-xml-threat-protection and mule-json-threat-protection plugins to pom.xml
 *
 * @author Mulesoft Inc.
 */
public class ThreatProtectionPomContributionMigrationStep implements PomContribution {

  private static final String COM_MULESOFT_ANYPOINT_GROUP_ID = "com.mulesoft.anypoint";
  private static final String MULE_XML_THREAT_PROTECTION_EXTENSION_ARTIFACT_ID = "mule-xml-threat-protection-extension";
  private static final String MULE_JSON_THREAT_PROTECTION_EXTENSION_ARTIFACT_ID = "mule-json-threat-protection-extension";
  private static final String XML_JSON_THREAT_PROTECTION_EXTENSION_VERSION_PROPERTY = "mule-threat-protection-extension";
  private static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";

  private final boolean isXmlThreatProtection;

  public ThreatProtectionPomContributionMigrationStep(boolean isXmlThreatProtection) {
    this.isXmlThreatProtection = isXmlThreatProtection;
  }

  @Override
  public String getDescription() {
    return "Pom contribution migration step for Client ID Enforcement policy";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport migrationReport) throws RuntimeException {
    pomModel.addDependency(new Dependency.DependencyBuilder()
        .withGroupId(COM_MULESOFT_ANYPOINT_GROUP_ID)
        .withArtifactId(isXmlThreatProtection ? MULE_XML_THREAT_PROTECTION_EXTENSION_ARTIFACT_ID
            : MULE_JSON_THREAT_PROTECTION_EXTENSION_ARTIFACT_ID)
        .withVersion(targetVersion(XML_JSON_THREAT_PROTECTION_EXTENSION_VERSION_PROPERTY))
        .withClassifier(MULE_PLUGIN_CLASSIFIER)
        .build());
  }
}
