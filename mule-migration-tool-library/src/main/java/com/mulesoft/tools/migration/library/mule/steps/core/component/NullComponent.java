/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.component;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate null-component to a raise-error
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class NullComponent extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("null-component");

  @Override
  public String getDescription() {
    return "Migrate null-component to a raise-error.";
  }

  public NullComponent() {
    this.setAppliedTo(XPATH_SELECTOR);
  }


  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.setName("raise-error");
    if (!object.getChildren().isEmpty()) {
      report.report("components.interceptors", object, object);
      object.removeContent();
    }
    object.setAttribute("type", "COMPATIBILITY:UNSUPPORTED");
    object.setAttribute("description", "This service cannot receive messages");
  }

}
