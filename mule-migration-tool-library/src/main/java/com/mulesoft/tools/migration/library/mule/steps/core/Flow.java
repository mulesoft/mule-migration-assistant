/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

<<<<<<< Upstream, based on origin/master
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.isErrorHanldingElement;
import static java.util.Collections.reverse;

=======
>>>>>>> f93e430 organize imports
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Content;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Migrate flow definitions
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Flow extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "/*/mule:flow";

  @Override
  public String getDescription() {
    return "Migrate flow definitions";
  }

  public Flow() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    element.setAttribute("name", element.getAttributeValue("name")
        .replaceAll("/", "\\\\")
        .replaceAll("\\[|\\{", "(")
        .replaceAll("\\]|\\}", ")")
        .replaceAll("#", "_"));

    if (element.getAttribute("processingStrategy") != null) {
      if ("synchronous".equals(element.getAttributeValue("processingStrategy"))) {
        element.setAttribute("maxConcurrency", "1");
      }

      element.removeAttribute("processingStrategy");
      report.report("flow.processingStrategy", element, element);
    }

    List<Element> responses = new ArrayList<>(element.getChildren("response", CORE_NAMESPACE));
    reverse(responses);

    for (Element response : responses) {
      Element wrappingTry = new Element("try", CORE_NAMESPACE);

      new ArrayList<>(element.getContent().subList(element.indexOf(response) + 1, element.getContentSize())).forEach(c -> {
        if (c instanceof Element && !isErrorHanldingElement((Element) c)) {
          c.detach();
          wrappingTry.addContent(c);
        }
      });

      addElementAfter(wrappingTry, response);

      List<Content> content = response.cloneContent();
      response.detach();
      element.addContent(content);
    }
  }

}
