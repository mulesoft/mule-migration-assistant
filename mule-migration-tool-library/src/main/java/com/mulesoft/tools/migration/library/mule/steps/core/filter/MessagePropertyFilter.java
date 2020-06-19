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
package com.mulesoft.tools.migration.library.mule.steps.core.filter;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate MessageProperty filter to a validation stub
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MessagePropertyFilter extends AbstractFilterMigrator {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("message-property-filter");

  @Override
  public String getDescription() {
    return "Update MessageProperty filter to a validation stub.";
  }

  public MessagePropertyFilter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    addValidationsModule(element.getDocument());

    String pattern = element.getAttributeValue("pattern");
    String scope = element.getAttributeValue("scope");
    String propertyName;
    String propertyValue;
    boolean negated = false;

    int x = pattern.indexOf(":");
    int i = pattern.indexOf('=');

    if (x > -1 && x < i) {
      scope = pattern.substring(0, x);
      pattern = pattern.substring(x + 1);
      i = pattern.indexOf('=');
    }

    if (pattern.charAt(i - 1) == '!') {
      negated = true;
      propertyName = pattern.substring(0, i - 1).trim();
    } else {
      propertyName = pattern.substring(0, i).trim();
    }
    propertyValue = pattern.substring(i + 1).trim();

    if (propertyValue.contains("*")) {
      if (negated) {
        element.setAttribute("regex", "(?!^" + propertyValue.replaceAll("\\*", ".*") + "$)");
      } else {
        element.setAttribute("regex", "^" + propertyValue.replaceAll("\\*", ".*") + "$");
      }
      element.removeAttribute("pattern");
      element.removeAttribute("scope");
      element.setAttribute("value",
                           "#[vars.compatibility_" + (scope == null ? "outbound" : scope) + "Properties['" + propertyName
                               + "']]");
      element.setName("matches-regex");
      element.setNamespace(VALIDATION_NAMESPACE);
    } else {
      element.setAttribute("expression",
                           "#[vars.compatibility_" + (scope == null ? "outbound" : scope) + "Properties['" + propertyName + "'] "
                               + (negated ? "!=" : "==") + " '" + propertyValue + "']");
      element.removeAttribute("scope");
      element.removeAttribute("pattern");
      element.setName("is-true");
      element.setNamespace(VALIDATION_NAMESPACE);
    }

    handleFilter(element);
  }

}
