/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
