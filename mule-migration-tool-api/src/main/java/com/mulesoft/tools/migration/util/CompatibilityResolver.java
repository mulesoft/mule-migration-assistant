/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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

  T resolve(T original, Element element, MigrationReport report, ApplicationModel model);
}
