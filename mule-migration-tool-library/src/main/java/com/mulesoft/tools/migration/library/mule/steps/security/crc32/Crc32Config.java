/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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

  private static final Namespace CRC32_NAMESPACE = getNamespace("crc32", "http://www.mulesoft.org/schema/mule/crc32");

  public static final String XPATH_SELECTOR = "//crc32:config";

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
