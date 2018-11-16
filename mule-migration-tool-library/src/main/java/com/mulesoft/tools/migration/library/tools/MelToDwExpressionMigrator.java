/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.tools;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addCompatibilityNamespace;
import static java.util.Objects.requireNonNull;

import com.mulesoft.tools.Migrator;
import com.mulesoft.tools.migration.library.tools.mel.MelCompatibilityResolver;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Migrate mel expressions to dw expression
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MelToDwExpressionMigrator implements ExpressionMigrator {

  private final MigrationReport report;

  private final Pattern EXPRESSION_WRAPPER = Pattern.compile("^\\s*#\\[(.*)]\\s*$", Pattern.DOTALL);
  private final Pattern EXPRESSION_TEMPLATE_WRAPPER = Pattern.compile(".*#\\[(.*)].*", Pattern.DOTALL);

  private final MelCompatibilityResolver compatibilityResolver = new MelCompatibilityResolver();
  private final ApplicationModel model;

  public MelToDwExpressionMigrator(MigrationReport report, ApplicationModel model) {
    this.report = report;
    this.model = model;
  }

  @Override
  public String migrateExpression(String originalExpression, boolean dataWeaveBodyOnly, Element element) {
    return migrateExpression(originalExpression, dataWeaveBodyOnly, element, false);
  }

  @Override
  public String migrateExpression(String originalExpression, boolean dataWeaveBodyOnly, Element element, boolean enricher) {
    if (!isWrapped(originalExpression) && !originalExpression.contains("#[")) {
      return originalExpression;
    }
    String unwrapped = unwrap(originalExpression);
    unwrapped = unwrapped.replaceAll("mel:", "");
    if (!unwrapped.contains("#[")) {
      return wrap(translateSingleExpression(unwrapped, dataWeaveBodyOnly, element, enricher));
    }
    // Probably an interpolation
    TemplateParser muleStyleParser = TemplateParser.createMuleStyleParser();
    String migratedExpression = muleStyleParser.translate(originalExpression,
                                                          (script) -> translateSingleExpression(script, dataWeaveBodyOnly,
                                                                                                element, enricher));
    if (migratedExpression.startsWith("#[mel:")) {
      addCompatibilityNamespace(element.getDocument());
    }
    return migratedExpression;
  }

  public String translateSingleExpression(String unwrappedExpression, boolean dataWeaveBodyOnly, Element element,
                                          boolean enricher) {
    String migratedExpression;
    try {
      migratedExpression = Migrator.migrate(unwrappedExpression);
    } catch (Exception e) {
      return compatibilityResolver.resolve(unwrappedExpression, element, report, model, this, enricher);
    }
    if (migratedExpression.contains("message.inboundAttachments")) {
      report.report("message.expressionsAttachments", element, element);
    }

    migratedExpression = resolveServerContext(migratedExpression);
    migratedExpression = resolveIdentifiers(migratedExpression);

    return dataWeaveBodyOnly ? migratedExpression.replaceFirst("%dw 2\\.0\n---", "").trim() : migratedExpression;
  }

  private String resolveServerContext(String expression) {
    return expression.replaceAll("(vars\\.)?server\\.dateTime", "now()")
        .replaceAll("(vars\\.)?server\\.nanoSeconds", "System.nanoTime()");
  }

  public String resolveIdentifiers(String expression) {
    return expression.replaceAll("flowVars", "vars")
        .replaceAll("recordVars", "vars")
        .replaceAll("message\\.id", "correlationId")
        .replaceAll("message\\.inboundProperties", "vars.compatibility_inboundProperties")
        .replaceAll("message\\.outboundProperties", "vars.compatibility_outboundProperties")
        .replaceAll("message\\.inboundAttachments", "payload.parts");
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

  @Override
  public boolean isTemplate(String originalExpression) {
    checkExpression(originalExpression);
    return EXPRESSION_TEMPLATE_WRAPPER.matcher(originalExpression).matches();
  }

  private void checkExpression(String expression) {
    requireNonNull(expression, "Expression cannot be null");
  }
}
