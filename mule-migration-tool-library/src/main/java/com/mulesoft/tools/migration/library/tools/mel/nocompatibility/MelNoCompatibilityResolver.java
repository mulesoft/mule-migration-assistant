/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.tools.mel.nocompatibility;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.CompatibilityResolver;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jdom2.Element;

/**
 * No compatibility mode resolver for general MEL expressions,
 * It works by using a list of resolvers and trying to solve the expression 
 * with all the matching resolvers
 *
 * @author Mulesoft Inc.
 * @since 1.4.0
 */
public class MelNoCompatibilityResolver implements CompatibilityResolver<NoCompatibilityResolverResult> {

  protected List<CompatibilityResolver<NoCompatibilityResolverResult>> resolvers;

  public MelNoCompatibilityResolver() {
    resolvers = new ArrayList<>();
    resolvers.add(new InboundPropertiesNoCompatibilityResolver());
    resolvers.add(new OutboundPropertiesNoCompatibilityResolver());
  }

  @Override
  public boolean canResolve(String original) {
    return true;
  }

  @Override
  public NoCompatibilityResolverResult resolve(String original, Element element, MigrationReport report, ApplicationModel model,
                                               ExpressionMigrator expressionMigrator) {
    return this.resolve(original, element, report, model, expressionMigrator, false);
  }

  @Override
  public NoCompatibilityResolverResult resolve(String original, Element element, MigrationReport report, ApplicationModel model,
                                               ExpressionMigrator expressionMigrator, boolean enricher) {
    List<CompatibilityResolver<NoCompatibilityResolverResult>> matchedResolvers = lookupResolvers(original);
    NoCompatibilityResolverResult resolverResult = null;
    String resolvedExpression = original;
    boolean markedAsSuccess = true;
    for (CompatibilityResolver<NoCompatibilityResolverResult> resolver : matchedResolvers) {
      resolverResult = resolver.resolve(resolvedExpression, element, report, model, expressionMigrator);
      if (!resolverResult.isSuccesful()) {
        markedAsSuccess = false;
      }
      resolvedExpression = resolverResult.getTranslation();
    }

    if (markedAsSuccess) {
      report.melExpressionSuccess(original);
    } else {
      report.melExpressionFailure(original);
    }

    return new NoCompatibilityResolverResult(resolvedExpression, markedAsSuccess);
  }

  protected List<CompatibilityResolver<NoCompatibilityResolverResult>> lookupResolvers(String original) {
    List<CompatibilityResolver<NoCompatibilityResolverResult>> matchedResolvers = resolvers.stream()
        .filter(r -> r.canResolve(original))
        .collect(Collectors.toList());
    return matchedResolvers;
  }
}
