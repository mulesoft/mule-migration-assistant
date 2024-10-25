/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.security.crc32;

import static java.util.Collections.singletonList;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Removes the reamining crc32 config.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Crc32Config extends AbstractApplicationModelMigrationStep {

  private static final String CRC32_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/crc32";

  private static final Namespace CRC32_NAMESPACE = getNamespace("crc32", CRC32_NAMESPACE_URI);

  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + CRC32_NAMESPACE_URI + "' and local-name()='config']";

  @Override
  public String getDescription() {
    return "Removes the reamining crc32 config.";
  }

  public Crc32Config() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(singletonList(CRC32_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    element.detach();
  }

}
