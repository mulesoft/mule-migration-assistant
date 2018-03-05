/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.step;

import com.mulesoft.tools.migration.engine.Executable;
import com.mulesoft.tools.migration.project.model.ApplicationModel;

/**
 * Building block of the execution engine
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public interface MigrationStep extends Executable {

  String getDescription();

  ApplicationModel getApplicationModel();

  void setApplicationModel(ApplicationModel applicationModel);

}
