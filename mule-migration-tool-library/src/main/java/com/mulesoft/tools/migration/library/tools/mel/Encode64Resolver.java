/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.tools.mel;

import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.CompatibilityResolver;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.Element;

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
