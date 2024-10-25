/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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

  CATALOG_FOLDER(File.separator + "catalog"),

  PROJECT(File.separator + ".project"),

  MULE(File.separator + ".mule"),

  SETTINGS(File.separator + ".settings"),

  MULE_PROJECT(File.separator + "mule-project.xml"),

  TARGET(File.separator + "target"),

  CLASSES(File.separator + "classes"),

  // Mule resources are processed elsewhere, not copied as they are

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
