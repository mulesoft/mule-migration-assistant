/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.tools.mel;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.CompatibilityResolver;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;

/**
 * Resolver for outbound properties message enrichers
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class OutboundPropertiesCompatibilityResolver implements CompatibilityResolver<String> {

  @Override
  public boolean canResolve(String original) {
    return original != null
        && (original.trim().toLowerCase().startsWith("header:outbound:")
            || (original.trim().toLowerCase().startsWith("header:") && original.split(":").length == 2));
  }

  @Override
  public String resolve(String original, Element element, MigrationReport report, ApplicationModel model,
                        ExpressionMigrator expressionMigrator) {
    return doResolve(original, report);
  }

  @Override
  public String resolve(String original, Element element, MigrationReport report, ApplicationModel model,
                        ExpressionMigrator expressionMigrator, boolean enricher) {
    String propertyName = doResolve(original, report);
    if (enricher) {
      return propertyName;
    } else {
      return "vars.compatibility_outboundProperties." + propertyName;
    }
  }

  private String doResolve(String original, MigrationReport report) {
    report.melExpressionSuccess(original);
    return original.trim().replaceFirst("(?i)^(header:outbound:|header:)", EMPTY);
  }

}
