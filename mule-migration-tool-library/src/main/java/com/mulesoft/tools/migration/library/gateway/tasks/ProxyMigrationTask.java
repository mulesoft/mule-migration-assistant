/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.tasks;

import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.gateway.steps.proxy.ApiTagMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.proxy.DWPropertyAttributeValueMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.proxy.DWWsdlPropertyAttributeValueMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.proxy.DescriptionTagMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.proxy.PropertyPlaceholderTagMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.proxy.ProxyRequestHeadersProcessorMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.proxy.ProxyResponseHeadersProcessorMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.proxy.TagsTagMigrationStep;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.Arrays;
import java.util.List;

/**
 * Proxy migration task
 *
 * @author Mulesoft Inc.
 */
public class ProxyMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Proxy migration task";
  }

  @Override
  public String getTo() {
    return MULE_4_VERSION;
  }

  @Override
  public String getFrom() {
    return MULE_3_VERSION;
  }

  @Override
  public List<MigrationStep> getSteps() {
    ProxyRequestHeadersProcessorMigrationStep requestStep = new ProxyRequestHeadersProcessorMigrationStep();
    requestStep.setApplicationModel(getApplicationModel());
    ProxyResponseHeadersProcessorMigrationStep responseStep = new ProxyResponseHeadersProcessorMigrationStep();
    responseStep.setApplicationModel(getApplicationModel());
    DWWsdlPropertyAttributeValueMigrationStep wsdlStep = new DWWsdlPropertyAttributeValueMigrationStep();
    wsdlStep.setApplicationModel(getApplicationModel());
    return Arrays.asList(
                         new DescriptionTagMigrationStep(),
                         new TagsTagMigrationStep(),
                         new ApiTagMigrationStep(),
                         new PropertyPlaceholderTagMigrationStep(),
                         requestStep,
                         responseStep,
                         wsdlStep,
                         new DWPropertyAttributeValueMigrationStep());
  }
}
