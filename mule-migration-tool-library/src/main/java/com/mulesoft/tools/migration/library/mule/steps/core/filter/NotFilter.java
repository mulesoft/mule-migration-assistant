/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.filter;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementsAfter;
import static java.util.stream.Collectors.toList;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;

import java.util.List;

/**
 * Migrate not-filter to validations
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class NotFilter extends AbstractFilterMigrator {

  public static final String XPATH_SELECTOR = "//*[local-name()='not-filter']";

  @Override
  public String getDescription() {
    return "Update not-filter to validations.";
  }

  public NotFilter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    if (element.getChildren().isEmpty()) {
      element.detach();
    } else {
      addValidationsModule(element.getDocument());

      List<Content> negated = element.cloneContent()
          .stream()
          .map(e -> e instanceof Element ? negateValidator((Element) e, report, element) : e)
          .collect(toList());
      addElementsAfter(negated, element);
      negated.forEach(e -> {
        if (e instanceof Element) {
          handleFilter((Element) e);
        }
      });
      element.detach();
    }
  }

  public Element negateValidator(Element validator, MigrationReport report, Element original) {
    if ("is-true".equals(validator.getName())) {
      validator.setName("is-false");
    } else if ("is-false".equals(validator.getName())) {
      validator.setName("is-true");
    } else if ("matches-regex".equals(validator.getName())) {
      Attribute regexAttr = validator.getAttribute("regex");

      if (regexAttr.getValue().startsWith("(?!") && regexAttr.getValue().endsWith(")")) {
        regexAttr.setValue(StringUtils.substring(regexAttr.getValue(), 3, -1));
      } else {
        regexAttr.setValue("(?!" + regexAttr.getValue() + ")");
      }
    } else if ("any".equals(validator.getName())) {
      validator.setName("all");
      validator.getChildren().forEach(c -> negateValidator(c, report, original));
    } else if ("all".equals(validator.getName())) {
      validator.setName("any");
      validator.getChildren().forEach(c -> negateValidator(c, report, original));
    } else {
      report.report(ERROR, original, validator, "Inner filter cannot be migrated to its negated version.");
    }

    return validator;
  }
}
