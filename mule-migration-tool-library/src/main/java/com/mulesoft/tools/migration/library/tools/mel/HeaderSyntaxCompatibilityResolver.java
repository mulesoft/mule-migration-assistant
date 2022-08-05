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

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

/**
 * Resolver for enrichers
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HeaderSyntaxCompatibilityResolver implements CompatibilityResolver<String> {

  private List<CompatibilityResolver<String>> resolvers;

  public HeaderSyntaxCompatibilityResolver() {
    resolvers = new ArrayList<>();
    resolvers.add(new InboundPropertiesCompatibilityResolver());
    resolvers.add(new OutboundPropertiesCompatibilityResolver());
    resolvers.add(new InvocationPropertiesCompatibilityResolver());
    resolvers.add(new SessionVariablesCompatibilityResolver());
    resolvers.add(new VariablesCompatibilityResolver());
    resolvers.add(new Encode64Resolver());
    resolvers.add(new FunctionExpressionEvaluatorResolver());
  }

  @Override
  public boolean canResolve(String original) {
    return resolvers.stream().anyMatch(r -> r.canResolve(original));
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
    return resolvers.stream()
        .filter(r -> r.canResolve(original))
        .findFirst().get();
  }
}
