/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.task;

import com.mulesoft.tools.migration.project.structure.ProjectType;

/**
 * Defines information to categorize e task
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
// TODO this may be annotations
public interface Categorizable {

  Version getTo();

  Version getFrom();

  ProjectType getProjectType();
}
