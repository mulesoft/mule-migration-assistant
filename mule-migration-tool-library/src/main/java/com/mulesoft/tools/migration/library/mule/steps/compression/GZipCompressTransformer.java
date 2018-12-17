/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.compression;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.removeAllAttributes;
import static java.lang.String.format;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrates Mule 3 gzip compress transformer
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class GZipCompressTransformer extends AbstractCompressionMigrationStep {

  private static final String ORIGINAL_ELEMENT_NAME = "gzip-compress-transformer";

  public GZipCompressTransformer() {
    setAppliedTo(getCoreXPathSelector(ORIGINAL_ELEMENT_NAME));
  }

  @Override
  public String getDescription() {
    return format("Update <%s /> to use the compression module", ORIGINAL_ELEMENT_NAME);
  }

  @Override
  protected String getStrategyName() {
    return "compressor";
  }

  @Override
  protected String getOperationName() {
    return "compress";
  }

  @Override
  protected void transformArguments(Element element, MigrationReport report) {
    super.transformArguments(element, report);
    removeAllAttributes(element);
  }
}
