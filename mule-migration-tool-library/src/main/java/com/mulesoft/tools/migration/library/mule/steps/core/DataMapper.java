/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.google.common.collect.Lists.newArrayList;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrate references of DataMapper
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class DataMapper extends AbstractApplicationModelMigrationStep {

  public static final String DATA_MAPPER_NAMESPACE = "http://www.mulesoft.org/schema/mule/ee/data-mapper";
  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + DATA_MAPPER_NAMESPACE + "']";

  public DataMapper() {
    this.setNamespacesContributions(newArrayList(Namespace.getNamespace("data-mapper", DATA_MAPPER_NAMESPACE)));
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    report.report("expressions.datamapper", object, object);
  }
}
