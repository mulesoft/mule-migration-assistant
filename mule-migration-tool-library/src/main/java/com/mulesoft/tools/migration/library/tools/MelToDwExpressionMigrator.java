/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.tools;

import static com.mulesoft.tools.migration.library.tools.PluginsVersions.targetVersion;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addCompatibilityNamespace;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import com.mulesoft.tools.*;
import com.mulesoft.tools.migration.library.tools.mel.DefaultMelCompatibilityResolver;
import com.mulesoft.tools.migration.library.tools.mel.MelCompatibilityResolver;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import scala.collection.JavaConverters;

import java.util.List;
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
    String migratedExpression;
    if (!unwrapped.contains("#[")) {
      migratedExpression = wrap(translateSingleExpression(unwrapped, dataWeaveBodyOnly, element, enricher));
    } else {
      // Probably an interpolation
      TemplateParser muleStyleParser = TemplateParser.createMuleStyleParser();
      migratedExpression = muleStyleParser.translate(originalExpression,
                                                     (script) -> translateSingleExpression(script, dataWeaveBodyOnly,
                                                                                           element, enricher));
      if (migratedExpression.startsWith("#[mel:")) {
        addCompatibilityNamespace(element.getDocument());
      }
    }

    return StringUtils.replaceAll(migratedExpression, "\\r\\n|[\\r\\n]", " ");
  }

  public String translateSingleExpression(String unwrappedExpression, boolean dataWeaveBodyOnly, Element element,
                                          boolean enricher) {
    String migratedExpression;
    MigrationResult result;
    try {
      result = Migrator.migrate(unwrappedExpression);
      migratedExpression = result.getGeneratedCode();
    } catch (Exception e) {
      return compatibilityResolver.resolve(unwrappedExpression, element, report, model, this, enricher);
    }
    if (result.metadata().children().exists(a -> a instanceof NonMigratable)) {
      List<NonMigratable> metadata =
          (List<NonMigratable>) (List<?>) JavaConverters.seqAsJavaList(result.metadata().children())
              .stream()
              .filter(a -> a instanceof NonMigratable)
              .collect(toList());

      metadata.forEach(a -> report.report(a.reason(), element, element));

      return new DefaultMelCompatibilityResolver().resolve(unwrappedExpression, element, report, model, this, enricher);
    }

    if (migratedExpression.contains("message.inboundAttachments")) {
      report.report("message.expressionsAttachments", element, element);
    }

    if (result.metadata().children().exists(a -> a instanceof JavaModuleRequired)) {
      Dependency javaModuleDependency = new Dependency.DependencyBuilder()
          .withGroupId("org.mule.module")
          .withArtifactId("mule-java-module")
          .withVersion(targetVersion("mule-java-module"))
          .build();
      model.getPomModel().ifPresent(m -> m.addDependency(javaModuleDependency));
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
        .replaceAll("message\\.inboundAttachments", "payload.parts")
        .replaceAll("message\\.dataType\\.mimeType", "message.^mediaType")
        .replaceAll("message\\.dataType\\.encoding", "message.^encoding");
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
