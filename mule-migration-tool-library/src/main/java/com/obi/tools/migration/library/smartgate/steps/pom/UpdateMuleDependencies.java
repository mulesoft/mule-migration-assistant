/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.steps.pom;

import java.util.List;

import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.Dependency.DependencyBuilder;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * update dependencies from pom.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class UpdateMuleDependencies implements PomContribution {

  @Override
  public String getDescription() {
    return "update Mule dependencies";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport report) {

    List<Dependency> dependencies = pomModel.getDependencies();

    dependencies.removeIf(d -> (d.getGroupId().startsWith("org.mule.modules") && d.getArtifactId().equals("mule-apikit-module")));
    pomModel.setDependencies(dependencies);
    dependencies = pomModel.getDependencies();
    dependencies.removeIf(d -> (d.getGroupId().startsWith("org.mule.connectors")
        && (d.getArtifactId().equals("mule-sockets-connector") || d.getArtifactId().equals("mule-http-connector"))));
    pomModel.setDependencies(dependencies);



    pomModel.addDependency(new DependencyBuilder().withGroupId("org.mule.modules").withArtifactId("mule-apikit-module")
        .withVersion("1.5.1").withClassifier("mule-plugin").build());

    pomModel.addDependency(new DependencyBuilder().withGroupId("org.mule.connectors").withArtifactId("mule-sockets-connector")
        .withVersion("1.2.1").withClassifier("mule-plugin").build());

    pomModel.addDependency(new DependencyBuilder().withGroupId("org.mule.connectors").withArtifactId("mule-http-connector")
        .withVersion("1.5.24").withClassifier("mule-plugin").build());

  }
}
