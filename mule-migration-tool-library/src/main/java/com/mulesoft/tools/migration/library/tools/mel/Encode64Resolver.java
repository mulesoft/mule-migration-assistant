/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.tools.mel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.Element;

import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.CompatibilityResolver;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

/**
 * Resolver for encodeBase64 method
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Encode64Resolver implements CompatibilityResolver<String> {

  private final Pattern base64Method =
      Pattern.compile("^\\s*org\\.apache\\.commons\\.codec\\.binary\\.Base64\\.encodeBase64\\s*\\((.*)?\\)\\s*$");

  @Override
  public boolean canResolve(String original) {
    return base64Method.matcher(original).matches();
  }

  @Override
  public String resolve(String original, Element element, MigrationReport report, ApplicationModel model,
                        ExpressionMigrator expressionMigrator) {
    original = original.trim();

    Matcher base64MethodMatcher = base64Method.matcher(original);
    if (base64MethodMatcher.matches() && base64MethodMatcher.groupCount() > 0) {
      if (base64MethodMatcher.group().equals(original)) {
        String innerExpression = base64MethodMatcher.group(1).replace(".getBytes()", "");
        innerExpression =
            ((MelToDwExpressionMigrator) expressionMigrator).translateSingleExpression(innerExpression, true, element, false);
        if (!innerExpression.startsWith("mel:")) {
          return "dw::core::Binaries::toBase64(" + innerExpression + ")";
        }
      }
    }
    report.report("expressions.encodeBase64", element, element);
    return original;
  }
}
