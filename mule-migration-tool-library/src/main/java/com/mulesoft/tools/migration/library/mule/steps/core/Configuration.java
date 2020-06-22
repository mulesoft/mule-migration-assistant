/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Element;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addCompatibilityNamespace;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getTopLevelCoreXPathSelector;

/**
 * Migrate Configuration element
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Configuration extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = getTopLevelCoreXPathSelector("configuration");

  @Override
  public String getDescription() {
    return "Update Configuration element.";
  }

  public Configuration() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    if (element.getAttribute("defaultProcessingStrategy") != null) {
      element.removeAttribute("defaultProcessingStrategy");
      report.report("configuration.processingStrategy", element, element);
    }

  }
}
