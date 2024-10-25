/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.component;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Remove echo-component
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class EchoComponent extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("echo-component");

  @Override
  public String getDescription() {
    return "Remove echo-component";
  }

  public EchoComponent() {
    this.setAppliedTo(XPATH_SELECTOR);
  }


  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    if (!object.getChildren().isEmpty()) {
      object.setName("set-payload");
      object.setAttribute("value", "#[payload]");
    } else {
      object.detach();
    }
  }

}
