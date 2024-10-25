/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.mule.steps.json.JsonMapper;
import com.mulesoft.tools.migration.library.mule.steps.json.JsonModulePomContribution;
import com.mulesoft.tools.migration.library.mule.steps.json.JsonSchemaValidationFilter;
import com.mulesoft.tools.migration.library.mule.steps.json.JsonToObjectTransformer;
import com.mulesoft.tools.migration.library.mule.steps.json.JsonToXmlTransformer;
import com.mulesoft.tools.migration.library.mule.steps.json.JsonValidateSchema;
import com.mulesoft.tools.migration.library.mule.steps.json.ObjectToJsonTransformer;
import com.mulesoft.tools.migration.library.mule.steps.json.XmlToJsonTransformer;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

/**
 * Migration Json module components
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class JsonMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Migrate Json module components";
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
    return newArrayList(new JsonModulePomContribution(),
                        new JsonValidateSchema(),
                        new JsonSchemaValidationFilter(),
                        new JsonToObjectTransformer(),
                        new ObjectToJsonTransformer(),
                        new JsonToXmlTransformer(),
                        new XmlToJsonTransformer(),
                        new JsonMapper());
  }
}
