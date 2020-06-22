/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
