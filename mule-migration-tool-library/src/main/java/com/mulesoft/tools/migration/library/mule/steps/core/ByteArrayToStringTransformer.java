/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * byte-array-to-string-transformer processor migration strategy
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ByteArrayToStringTransformer extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("byte-array-to-string-transformer");

  public ByteArrayToStringTransformer() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public String getDescription() {
    return "Mark byte-array-to-string-transformer processor as not supported for migration";
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Element parentElement = object.getParentElement();
    object.detach();
    report.report("expressionTransformer.deprecated", object, parentElement);
  }


}
