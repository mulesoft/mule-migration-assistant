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

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementsAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Content;
import org.jdom2.Element;

import java.util.List;

/**
 * Migrate message filters
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MessageFilter extends AbstractFilterMigrator {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("message-filter");

  @Override
  public String getDescription() {
    return "Update message-filters.";
  }

  public MessageFilter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    if (element.getParentElement().isRootElement()) {
      // Nothing to do, this will be removed
      return;
    }

    if (element.getAttribute("onUnaccepted") != null) {
      Element wrappingTry = new Element("try", CORE_NAMESPACE);

      if (element.getAttribute("throwOnUnaccepted") != null && element.getAttributeValue("throwOnUnaccepted").equals("false")) {
        report.report("filters.validationsRaiseError", element, wrappingTry);
      }
      element.removeAttribute("throwOnUnaccepted");

      addElementAfter(wrappingTry, element);
      wrappingTry.addContent(element.cloneContent());

      wrappingTry.addContent(new Element("error-handler", CORE_NAMESPACE)
          .addContent(new Element("on-error-propagate", CORE_NAMESPACE)
              .setAttribute("type", "MULE:VALIDATION")
              .setAttribute("logException", "false")
              .addContent(new Element("flow-ref", CORE_NAMESPACE)
                  .setAttribute("name", element.getAttributeValue("onUnaccepted")))));

    } else {
      List<Content> clonedContent = element.cloneContent();

      if (element.getAttribute("throwOnUnaccepted") == null || element.getAttributeValue("throwOnUnaccepted").equals("false")) {
        report.report("filters.validationsRaiseError", element,
                      (Element) clonedContent.stream().filter(c -> c instanceof Element).findFirst().get());
      }
      element.removeAttribute("throwOnUnaccepted");

      addElementsAfter(clonedContent, element);
    }

    element.detach();
  }
}
