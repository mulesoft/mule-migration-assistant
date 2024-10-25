/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.compression;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;
import static java.lang.String.format;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Migrates Mule 3 gzip uncompress transformer
 *
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class GZipUncompressTransformer extends AbstractCompressionMigrationStep {

  public static final String ORIGINAL_ELEMENT_NAME = "gzip-uncompress-transformer";
  public static final String XPATH_SELECTOR = getCoreXPathSelector(ORIGINAL_ELEMENT_NAME);

  public GZipUncompressTransformer() {
    setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public String getDescription() {
    return format("Update <%s /> to use the compression module", ORIGINAL_ELEMENT_NAME);
  }

  @Override
  protected String getStrategyName() {
    return "decompressor";
  }

  @Override
  protected String getOperationName() {
    return "decompress";
  }

  @Override
  protected void transformArguments(Element element, MigrationReport report) {
    super.transformArguments(element, report);

    Attribute mimeType = element.getAttribute("mimeType");
    if (mimeType != null) {
      mimeType.setName("outputMimeType");
    }

    Attribute encoding = element.getAttribute("encoding");
    if (encoding != null) {
      encoding.setName("outputEncoding");
    }
  }
}
