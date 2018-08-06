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

/**
 * Files/Folders excluded from migration.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public enum Exclusions {

  CLASSPATH(".classpath"), GIT(".gitignore"), PROJECT(".project"), MULE(".mule"), SETTINGS(".settings"), CATALOG(
      "catalog"), MULE_PROJECT("mule-project.xml"), TARGET("target"), MULE_THREE_APP(
          MuleThreeApplication.srcMainConfigurationPath), MULE_THREE_TEST(
              MuleThreeApplication.srcTestsConfigurationPath), MULE_FOUR_APP(
                  MuleFourApplication.srcMainConfigurationPath), MULE_FOUR_TEST(
                      MuleFourApplication.srcTestConfigurationPath), MULE_THREE_DOMAIN(
                          MuleThreeDomain.srcMainConfigurationPath), MULE_FOUR_DOMAIN(MuleFourDomain.srcMainConfigurationPath);

  private String exclusion;

  Exclusions(String exclusion) {
    this.exclusion = exclusion;
  }

  public String exclusion() {
    return exclusion;
  }

}
