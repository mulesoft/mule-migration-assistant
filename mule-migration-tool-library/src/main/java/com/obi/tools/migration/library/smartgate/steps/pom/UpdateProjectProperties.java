/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.steps.pom;

import java.util.Properties;

import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * UpdateProjectProperties remove "mule.version" and add "app.runtime"
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class UpdateProjectProperties implements PomContribution {


  @Override
  public String getDescription() {
    return "Update project version";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport report) throws RuntimeException {
    Properties properties = pomModel.getProperties();
    properties.remove("mule.version");
    properties.put("app.runtime", "4.3.0"); // TODO Configable
    pomModel.setProperties(properties);
  }
}
