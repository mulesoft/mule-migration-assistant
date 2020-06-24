/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
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
