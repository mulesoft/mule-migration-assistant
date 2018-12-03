/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.project.structure.util;

import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourApplication;
import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourDomain;
import com.mulesoft.tools.migration.engine.project.structure.mule.three.MuleThreeApplication;
import com.mulesoft.tools.migration.engine.project.structure.mule.three.MuleThreeDomain;

import java.io.File;

/**
 * Files/Folders excluded from migration.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public enum Exclusions {

  CLASSPATH(File.separator + ".classpath"),

  GIT(File.separator + ".gitignore"),

  PROJECT(File.separator + ".project"),

  MULE(File.separator + ".mule"),

  SETTINGS(File.separator + ".settings"),

  CATALOG(File.separator + "catalog"),

  MULE_PROJECT(File.separator + "mule-project.xml"),

  TARGET(File.separator + "target"),

  CLASSES(File.separator + "classes"),

  // Mule resources are processed elsewhere, not cpioed as they are

  MULE_THREE_APP(File.separator + MuleThreeApplication.srcMainConfigurationPath),

  MULE_THREE_TEST(File.separator + MuleThreeApplication.srcTestsConfigurationPath),

  MULE_FOUR_APP(File.separator + MuleFourApplication.srcMainConfigurationPath),

  MULE_FOUR_TEST(File.separator + MuleFourApplication.srcTestConfigurationPath),

  MULE_THREE_DOMAIN(File.separator + MuleThreeDomain.srcMainConfigurationPath),

  MULE_FOUR_DOMAIN(File.separator + MuleFourDomain.srcMainConfigurationPath);

  private String exclusion;

  Exclusions(String exclusion) {
    this.exclusion = exclusion;
  }

  public String exclusion() {
    return exclusion;
  }

}
