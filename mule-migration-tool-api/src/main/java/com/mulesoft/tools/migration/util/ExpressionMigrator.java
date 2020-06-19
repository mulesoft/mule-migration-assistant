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
package com.mulesoft.tools.migration.util;

import org.jdom2.Element;

/**
 * An interface to handle the migration of expressions.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public interface ExpressionMigrator {

  /**
   * Converts a Mule 3 expression to its Mule 4 equivalent.
   *
   * @return an expression that works in Mule 4
   */
  String migrateExpression(String originalExpression, boolean dataWeaveBodyOnly, Element element);

  /**
   * Converts a Mule 3 expression to its Mule 4 equivalent.
   *
   * @return an expression that works in Mule 4
   */
  default String migrateExpression(String originalExpression, boolean dataWeaveBodyOnly, Element element, boolean enricher) {
    return migrateExpression(originalExpression, dataWeaveBodyOnly, element);
  }

  /**
   * Removes the '#[' prefix and ']' suffix from an expression.
   * <p>
   * If the expression is not wrapped, it is returned unchanged,
   *
   * @return
   */
  String unwrap(String originalExpression);

  /**
   * Adds the '#[' prefix and ']' suffix to an expression
   * <p>
   * If the expression is already wrapped, it is returned unchanged,
   *
   * @return
   */
  String wrap(String originalExpression);

  /**
   * Checks if the expression has the '#[' prefix and ']' suffix
   *
   * @return
   */
  boolean isWrapped(String originalExpression);

  /**
   * Checks if the expression contains the '#[' prefix and ']' suffix pairs inside
   *
   * @return
   */
  boolean isTemplate(String originalExpression);
}
