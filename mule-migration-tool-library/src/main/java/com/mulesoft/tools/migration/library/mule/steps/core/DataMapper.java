/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
