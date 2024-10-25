/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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
