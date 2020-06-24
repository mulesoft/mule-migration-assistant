/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.json;

import static com.google.common.collect.Lists.newArrayList;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Remove JSON transformation mappers
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class JsonMapper extends AbstractApplicationModelMigrationStep implements JsonMigrationStep {

  public static final String XPATH_SELECTOR = "/*/*[namespace-uri()='" + JSON_NAMESPACE_URI + "'"
      + " and local-name()='mapper']";

  @Override
  public String getDescription() {
    return "Remove JSON transformation mappers";
  }

  public JsonMapper() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(JSON_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    report.report("json.mapper", element, element.getParentElement());
    element.detach();
  }

}
