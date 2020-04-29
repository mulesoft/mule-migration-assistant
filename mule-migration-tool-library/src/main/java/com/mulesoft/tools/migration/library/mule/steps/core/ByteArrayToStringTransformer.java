/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
