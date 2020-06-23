/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.cxf;

import static com.mulesoft.tools.migration.library.tools.PluginsVersions.targetVersion;

import com.mulesoft.tools.migration.project.model.pom.Dependency.DependencyBuilder;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Changes the namespace of the elements of the CXF module
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CxfModuleNamespaceMigrator extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = "//*[namespace-uri()='http://www.mulesoft.org/schema/mule/cxf']";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update CXF module config.";
  }

  public CxfModuleNamespaceMigrator() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    getApplicationModel().getPomModel().get().addDependency(new DependencyBuilder()
        .withGroupId("com.mulesoft.mule.modules")
        .withArtifactId("mule-compatibility-module")
        .withVersion(targetVersion("mule-compatibility-module"))
        .withClassifier("mule-plugin")
        .build());

    getApplicationModel().addNameSpace(Namespace.getNamespace("cxf", "http://www.mulesoft.org/schema/mule/cxf"),
                                       "http://www.mulesoft.org/schema/mule/cxf/current/mule-cxf.xsd",
                                       object.getDocument());
    object.setNamespace(Namespace.getNamespace("cxf", "http://www.mulesoft.org/schema/mule/cxf"));

  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }
}
