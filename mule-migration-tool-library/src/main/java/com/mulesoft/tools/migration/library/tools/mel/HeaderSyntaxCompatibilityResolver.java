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
 * Resolver for enrichers
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HeaderSyntaxCompatibilityResolver implements CompatibilityResolver<String> {

  private static List<CompatibilityResolver<String>> resolvers;

  static {
    resolvers = new ArrayList<>();
    resolvers.add(new InboundPropertiesCompatibilityResolver());
    resolvers.add(new OutboundPropertiesCompatibilityResolver());
  }


  @Override
  public boolean canResolve(String original) {
    return resolvers.stream().anyMatch(r -> r.canResolve(original));
  }

  @Override
  public String resolve(String original, Element element, MigrationReport report, ApplicationModel model) {
    return resolvers.stream()
        .filter(r -> r.canResolve(original))
        .findFirst().get()
        .resolve(original, element, report, model);
  }
}
