/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import com.mulesoft.tools.migration.library.mule.steps.core.ByteArrayToStringTransformer;
import com.mulesoft.tools.migration.library.mule.steps.xml.XmlToDomTransformer;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

/**
 * Migration definition for deprecated/removed Mule Transformers
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class TransformersMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Migrate deprecated/removed transformers";
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
    return newArrayList(new XmlToDomTransformer(),
                        new ByteArrayToStringTransformer());
  }
}
