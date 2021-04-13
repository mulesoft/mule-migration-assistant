/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.tools.mel;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.CompatibilityResolver;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;

/**
 * Resolver for inbound properties message enrichers
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class InboundAttachmentsCompatibilityResolver implements CompatibilityResolver<String> {

  @Override
  public boolean canResolve(String original) {
    return original != null && original.contains("message.inboundAttachments");
  }

  @Override
  public String resolve(String original, Element element, MigrationReport report, ApplicationModel model,
                        ExpressionMigrator expressionMigrator) {
    report.report("expressions.inboundAttachments", element, element);

    return "mel:" + original;
  }

}
