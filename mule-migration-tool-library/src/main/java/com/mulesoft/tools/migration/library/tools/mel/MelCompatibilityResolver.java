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

import java.util.ArrayList;
import java.util.List;

/**
 * Compatibility resolver for general MEL expressions
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MelCompatibilityResolver implements CompatibilityResolver<String> {

  private static List<CompatibilityResolver<String>> resolvers;

  static {
    resolvers = new ArrayList<>();
    resolvers.add(new EnrichersCompatibilityResolver());
  }

  @Override
  public boolean canResolve(String original) {
    return true;
  }

  @Override
  public String resolve(String original, Element element, MigrationReport report, ApplicationModel model) {
    CompatibilityResolver<String> resolver = resolvers.stream()
        .filter(r -> r.canResolve(original))
        .findFirst()
        .orElse(new DefaultMelCompatibilityResolver());

    return resolver.resolve(original, element, report, model);
  }
}
