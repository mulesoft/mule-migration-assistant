/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.task;


import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_DOMAIN;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_POLICY;

import com.mulesoft.tools.migration.project.ProjectType;

import java.util.HashSet;
import java.util.Set;

/**
 * Defines information to categorize e task
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
// TODO this may be annotations
public interface Categorizable {

  String getTo();

  String getFrom();

  default Set<ProjectType> getApplicableProjectTypes() {
    Set<ProjectType> types = new HashSet<>();
    types.add(MULE_FOUR_APPLICATION);
    types.add(MULE_FOUR_DOMAIN);
    types.add(MULE_FOUR_POLICY);
    return types;
  }

  @Deprecated
  default ProjectType getProjectType() {
    return MULE_FOUR_APPLICATION;
  }
}
