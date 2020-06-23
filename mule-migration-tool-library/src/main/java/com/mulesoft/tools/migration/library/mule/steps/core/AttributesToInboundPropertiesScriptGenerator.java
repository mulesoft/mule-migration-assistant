/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.library.mule.steps.core.properties.InboundPropertiesHelper.aggregateAttributesMapping;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.ProjectStructureContribution;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Aggregate all attributes mappings in a single script
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class AttributesToInboundPropertiesScriptGenerator implements ProjectStructureContribution {

  @Override
  public String getDescription() {
    return "Aggregate all attributes mappings in a single script.";
  }

  @Override
  public void execute(Path basePath, MigrationReport report) throws RuntimeException {
    try {
      aggregateAttributesMapping(basePath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
