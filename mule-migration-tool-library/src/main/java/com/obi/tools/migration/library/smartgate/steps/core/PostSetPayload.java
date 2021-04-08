/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.steps.core;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Post Migrate set-payload empty mimetype
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class PostSetPayload extends AbstractApplicationModelMigrationStep {

  private static final String NULL = "null";
  private static final String MIMETYPE = "mimeType";
  public static final String XPATH_SELECTOR = getCoreXPathSelector("set-payload");

  @Override
  public String getDescription() {
    return "ost Migrate set-payload  empty mimetype";
  }

  public PostSetPayload() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {

    if (NULL.equals(element.getAttributeValue(MIMETYPE))) {
      element.removeAttribute(MIMETYPE);
    }
  }

}
