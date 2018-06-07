/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.tools.mel;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.CompatibilityResolver;
import org.jdom2.Element;
import static org.apache.commons.lang3.StringUtils.EMPTY;

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
  public String resolve(String original, Element element, MigrationReport report, ApplicationModel model) {
    String propertyName = original.trim().replaceFirst("(?i)^(header:outbound:|header:)", EMPTY);
    return propertyName;
  }
}
