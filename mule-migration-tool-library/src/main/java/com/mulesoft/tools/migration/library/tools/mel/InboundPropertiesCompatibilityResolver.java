/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.tools.mel;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.CompatibilityResolver;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.Element;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Resolver for inbound properties message enrichers
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class InboundPropertiesCompatibilityResolver implements CompatibilityResolver<String> {

  @Override
  public boolean canResolve(String original) {
    return original != null && original.trim().toLowerCase().startsWith("header:inbound:");
  }

  @Override
  public String resolve(String original, Element element, MigrationReport report, ApplicationModel model,
                        ExpressionMigrator expressionMigrator) {
    String propertyName = original.trim().replaceFirst("(?i)^header:inbound:", EMPTY);
    return "vars.compatibility_inboundProperties." + propertyName;
  }
}
