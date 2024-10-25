/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.basicstructure;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_3_POLICY_NAMESPACE;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.List;

import org.jdom2.Element;

/**
 * Migrate pointcut elements
 *
 * @author Mulesoft Inc.
 */
public class PointcutTagMigrationStep extends AbstractBasicStructureMigrationStep {

  private static final String POINTCUT_TAG_NAME = "pointcut";
  private static final String API_POINTCUT_TAG_NAME = "api-pointcut";
  private static final String RESOURCE_TAG_NAME = "resource";
  private static final String ENDPOINT_TAG_NAME = "endpoint";
  private static final String APP_TAG_NAME = "app";

  public PointcutTagMigrationStep() {
    super(MULE_3_POLICY_NAMESPACE, POINTCUT_TAG_NAME);
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    List<Element> children = element.getChildren();
    if (!children.isEmpty()) {
      children.stream().forEach(e -> {
        switch (e.getName()) {
          case APP_TAG_NAME:
            migrationReport.report("basicStructure.pointcutMigrationStepNoEquivalent", e, e, "App");
            break;
          case ENDPOINT_TAG_NAME:
            migrationReport.report("basicStructure.pointcutMigrationStepNoEquivalent", e, e, "Endpoint");
            break;
          case RESOURCE_TAG_NAME:
            migrationReport.report("basicStructure.pointcutMigrationStepResolvedByRuntime", e, e, "Resource");
            break;
          case API_POINTCUT_TAG_NAME:
            migrationReport.report("basicStructure.pointcutMigrationStepResolvedByRuntime", e, e, "Api");
            break;
          default:
            migrationReport.report("basicStructure.pointcutMigrationStepUnknown", e, e, "Unknown");
            break;
        }
      });
    } else {
      migrationReport.report("basicStructure.pointcutMigrationStepUnknown", element, element, "Unknown");
    }
    detachContent(element.getContent());
    setUpHttpPolicy(element, true, migrationReport);
  }
}
