/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project;

/**
 * List the possible project types
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public enum ProjectType {
  BASIC, JAVA,

  MULE_FOUR_APPLICATION("mule-application"),

  MULE_THREE_APPLICATION(MULE_FOUR_APPLICATION, "mule-application"),

  MULE_THREE_MAVEN_APPLICATION(MULE_FOUR_APPLICATION, "mule-application"),

  MULE_FOUR_DOMAIN("mule-domain"),

  MULE_THREE_DOMAIN(MULE_FOUR_DOMAIN, "mule-domain"),

  MULE_THREE_MAVEN_DOMAIN(MULE_FOUR_DOMAIN, "mule-domain"),

  MULE_FOUR_POLICY("mule-policy"),

  MULE_THREE_POLICY(MULE_FOUR_POLICY, "mule-policy");

  private ProjectType targetType;
  private String packaging;

  private ProjectType() {
    this.targetType = null;
  }

  private ProjectType(String packaging) {
    this.packaging = packaging;
  }

  private ProjectType(ProjectType targetType, String packaging) {
    this.targetType = targetType;
    this.packaging = packaging;
  }

  public ProjectType getTargetType() {
    return targetType;
  }

  public String getPackaging() {
    return packaging;
  }
}
