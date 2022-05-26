/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.tools.mel.nocompatibility;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.applicationgraph.ApplicationGraph;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.CompatibilityResolver;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * No compatibility mode resolver for general MEL expressions
 *
 * @author Mulesoft Inc.
 * @since 1.4.0
 */
public class MelNoCompatibilityResolver implements CompatibilityResolver<String> {

  protected List<CompatibilityResolver<String>> resolvers;
  private ApplicationGraph graph;

  public MelNoCompatibilityResolver(ApplicationGraph graph) {
    resolvers = new ArrayList<>();
    resolvers.add(new InboundPropertiesNoCompatibilityResolver());
    resolvers.add(new OutboundPropertiesNoCompatibilityResolver());
    this.graph = graph;
  }

  @Override
  public boolean canResolve(String original) {
    return graph != null;
  }

  @Override
  public String resolve(String original, Element element, MigrationReport report, ApplicationModel model,
                        ExpressionMigrator expressionMigrator) {
    return lookupResolver(original).resolve(original, element, report, model, expressionMigrator);
  }

  @Override
  public String resolve(String original, Element element, MigrationReport report, ApplicationModel model,
                        ExpressionMigrator expressionMigrator, boolean enricher) {
    return lookupResolver(original).resolve(original, element, report, model, expressionMigrator, enricher);
  }

  protected CompatibilityResolver<String> lookupResolver(String original) {
    CompatibilityResolver<String> resolver = resolvers.stream()
        .filter(r -> r.canResolve(original))
        .findFirst()
        .orElse(new EmptyNoCompatibilityResolver());
    return resolver;
  }
}
