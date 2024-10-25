/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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

  public static final String ORIGINAL_ELEMENT_NAME = "gzip-compress-transformer";
  public static final String XPATH_SELECTOR = getCoreXPathSelector(ORIGINAL_ELEMENT_NAME);

  public GZipCompressTransformer() {
    setAppliedTo(XPATH_SELECTOR);
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
