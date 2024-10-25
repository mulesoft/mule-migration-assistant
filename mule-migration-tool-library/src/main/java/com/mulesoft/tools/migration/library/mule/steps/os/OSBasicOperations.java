/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.os;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Element;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Migration of basic operations.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class OSBasicOperations extends AbstractOSMigrator {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri() = 'http://www.mulesoft.org/schema/mule/objectstore' and " +
          "(local-name() = 'contains' or local-name() = 'retrieve-all-keys' or local-name() = 'remove')]";

  public OSBasicOperations() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(OS_NAMESPACE, NEW_OS_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    migrateOS(element);
  }
}
