/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
