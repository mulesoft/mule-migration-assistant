/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.project.version;

import org.junit.Test;

import static com.mulesoft.tools.migration.util.version.VersionUtils.isVersionGreaterOrEquals;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class VersionUtilsTest {

  @Test
  public void isVersionGraterOrEqualsVersion1() throws Exception {
    String version1 = "3.3.9";
    String version2 = "3.3.*";

    assertThat(version1 + " is not greater than " + version2, isVersionGreaterOrEquals(version1, version2), is(true));
  }

  @Test
  public void isVersionGraterOrEqualsVersion2() throws Exception {
    String version1 = "3.3.9";
    String version2 = "3.4.*";

    assertThat(version1 + " is not greater than " + version2, isVersionGreaterOrEquals(version1, version2), is(false));
  }

  @Test
  public void isVersionGraterOrEqualsVersion3() throws Exception {
    String version1 = "3.3.9";
    String version2 = "3.*.*";

    assertThat(version1 + " is not greater than " + version2, isVersionGreaterOrEquals(version1, version2), is(true));
  }

  @Test
  public void isVersionGraterOrEqualsVersion4() throws Exception {
    String version1 = "3.8";
    String version2 = "3.8.1";

    assertThat(version1 + " is not greater than " + version2, isVersionGreaterOrEquals(version1, version2), is(false));
  }

  @Test
  public void isVersionGraterOrEqualsVersion5() throws Exception {
    String version1 = "3.8";
    String version2 = "3.8.0";

    assertThat(version1 + " is not greater than " + version2, isVersionGreaterOrEquals(version1, version2), is(true));
  }

  @Test
  public void isVersionGraterOrEqualsVersion6() throws Exception {
    String version1 = "4.1.3-SNAPSHOT";
    String version2 = "4.1.2";

    assertThat(version1 + " is not greater than " + version2, isVersionGreaterOrEquals(version1, version2), is(true));
  }

}
