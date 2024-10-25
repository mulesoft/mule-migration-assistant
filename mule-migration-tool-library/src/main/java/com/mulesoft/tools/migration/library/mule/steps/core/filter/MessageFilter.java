/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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
