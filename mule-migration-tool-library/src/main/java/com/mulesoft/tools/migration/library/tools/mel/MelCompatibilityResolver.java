/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.tools.mel;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.applicationgraph.ApplicationGraph;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.CompatibilityResolver;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Compatibility resolver for general MEL expressions. 
 * Only one resolver will apply per expression
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MelCompatibilityResolver implements CompatibilityResolver<String> {

  private List<CompatibilityResolver<String>> resolvers;
  private ApplicationGraph applicationGraph;

  public MelCompatibilityResolver(ApplicationGraph applicationGraph) {
    resolvers = new ArrayList<>();
    resolvers.add(new InboundAttachmentsCompatibilityResolver());
    resolvers.add(new HeaderSyntaxCompatibilityResolver());
    this.applicationGraph = applicationGraph;
  }

  @Override
  public boolean canResolve(String original) {
    return true;
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
        .orElse(new DefaultMelCompatibilityResolver());
    return resolver;
  }
}
