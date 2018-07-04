/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.tools;

import com.mulesoft.tools.Migrator;
import com.mulesoft.tools.migration.library.tools.mel.MelCompatibilityResolver;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;

/**
 * Migrate mel expressions to dw expression
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MelToDwExpressionMigrator implements ExpressionMigrator {

  private final MigrationReport report;

  private final Pattern EXPRESSION_WRAPPER = Pattern.compile("^\\s*#\\[(.*)]\\s*$", Pattern.DOTALL);

  private final MelCompatibilityResolver compatibilityResolver = new MelCompatibilityResolver();
  private final ApplicationModel model;

  public MelToDwExpressionMigrator(MigrationReport report, ApplicationModel model) {
    this.report = report;
    this.model = model;
  }

  @Override
  public String migrateExpression(String originalExpression, boolean dataWeaveBodyOnly, Element element) {
    if (!isWrapped(originalExpression) && !originalExpression.contains("#[")) {
      return originalExpression;
    }
    String unwrapped = unwrap(originalExpression);
    unwrapped = unwrapped.replaceAll("mel:", "");
    if (!unwrapped.contains("#[")) {
      return wrap(translateSingleExpression(unwrapped, dataWeaveBodyOnly, element));
    }
    // Probably an interpolation
    TemplateParser muleStyleParser = TemplateParser.createMuleStyleParser();
    return muleStyleParser.translate(originalExpression,
                                     (script) -> translateSingleExpression(script, dataWeaveBodyOnly, element));
  }

  private String translateSingleExpression(String unwrappedExpression, boolean dataWeaveBodyOnly, Element element) {
    String migratedExpression;
    try {
      migratedExpression = Migrator.migrate(unwrappedExpression);
    } catch (Exception e) {
      return compatibilityResolver.resolve(unwrappedExpression, element, report, model);
    }
    migratedExpression = resolveServerContext(migratedExpression);
    migratedExpression = resolveIdentifiers(migratedExpression);
    return dataWeaveBodyOnly ? migratedExpression.replaceFirst("---", "").trim() : migratedExpression;
  }

  private String resolveServerContext(String expression) {
    return expression.replaceAll("(vars\\.)?server\\.dateTime", "now()").replaceAll("(vars\\.)?server\\.nanoSeconds",
                                                                                    "System.nanoTime()");
  }

  public String resolveIdentifiers(String expression) {
    return expression.replaceAll("flowVars", "vars").replaceAll("message\\.inboundProperties",
                                                                "vars.compatibility_inboundProperties");
  }

  @Override
  public String unwrap(String originalExpression) {
    checkExpression(originalExpression);
    Matcher wrappedExpressionMatcher = EXPRESSION_WRAPPER.matcher(originalExpression);
    if (wrappedExpressionMatcher.matches()) {
      return unwrap(wrappedExpressionMatcher.group(1).trim());
    }
    return originalExpression;
  }

  @Override
  public String wrap(String originalExpression) {
    checkExpression(originalExpression);
    return isWrapped(originalExpression) ? originalExpression : "#[" + originalExpression + "]";
  }

  @Override
  public boolean isWrapped(String originalExpression) {
    checkExpression(originalExpression);
    return EXPRESSION_WRAPPER.matcher(originalExpression).matches();
  }

  private void checkExpression(String expression) {
    checkArgument(expression != null, "Expression cannot be null");
  }
}
