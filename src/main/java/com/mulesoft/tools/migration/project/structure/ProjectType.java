/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.structure;

/**
 * List the possible project types
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public enum ProjectType {
  BASIC, JAVA,

  MULE_THREE_APPLICATION, MULE_THREE_MAVEN_APPLICATION, MULE_FOUR_APPLICATION,

  MULE_THREE_DOMAIN, MULE_THREE_MAVEN_DOMAIN, MULE_FOUR_DOMAIN;
}
