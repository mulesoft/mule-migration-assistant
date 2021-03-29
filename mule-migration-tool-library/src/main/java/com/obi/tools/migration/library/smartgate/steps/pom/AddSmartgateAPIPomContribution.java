/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.steps.pom;

import com.mulesoft.tools.migration.project.model.pom.Dependency.DependencyBuilder;

import java.util.Properties;

import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Adds APIkit dependency
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class AddSmartgateAPIPomContribution implements PomContribution {


  private String OBI_SMARTGATE_ANYPOINT_EXCHANGE_ASSET_VERSION = "obi.smartgate.anypoint.exchange.asset.version";
  private String OBI_SMARTGATE_ANYPOINT_EXCHANGE_ASSET_BUSINESSGROUPID = "obi.smartgate.anypoint.exchange.asset.businessgroupid";

  @Override
  public String getDescription() {
    return "Add Project API dependency";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport report) throws RuntimeException {

    String tArtifactId = pomModel.getArtifactId();
    Properties properties = pomModel.getProperties();
    String tempBG = properties.getProperty(OBI_SMARTGATE_ANYPOINT_EXCHANGE_ASSET_BUSINESSGROUPID);
    String tempAssetVersion = properties.getProperty(OBI_SMARTGATE_ANYPOINT_EXCHANGE_ASSET_VERSION);

    Dependency dependency = new DependencyBuilder()
        .withGroupId(tempBG)
        .withArtifactId(tArtifactId)
        .withVersion(tempAssetVersion)
        .withClassifier("raml")
        .withType("zip")
        .build();
    pomModel.addDependency(dependency);
  }
}
