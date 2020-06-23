/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.util;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Comptibility resolver
 *
 * @param <T>
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public interface CompatibilityResolver<T> {

  boolean canResolve(T original);

  T resolve(String original, Element element, MigrationReport report, ApplicationModel model,
            ExpressionMigrator expressionMigrator);

  default T resolve(String original, Element element, MigrationReport report, ApplicationModel model,
                    ExpressionMigrator expressionMigrator, boolean enricher) {
    return resolve(original, element, report, model, expressionMigrator);
  }
}
