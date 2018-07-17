/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.project.version;

import com.mulesoft.tools.migration.exception.MigrationException;
import org.junit.Test;

import static com.mulesoft.tools.migration.util.version.VersionUtils.MIN_MULE4_VALID_VERSION;
import static com.mulesoft.tools.migration.util.version.VersionUtils.isVersionGreaterOrEquals;
import static com.mulesoft.tools.migration.util.version.VersionUtils.isVersionValid;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class VersionUtilsTest {

  @Test
  public void isVersionGreaterOrEqualsVersion1() throws Exception {
    String version1 = "3.3.9";
    String version2 = "3.3.*";

    assertThat(version1 + " is not greater than " + version2, isVersionGreaterOrEquals(version1, version2), is(true));
  }

  @Test
  public void isVersionGreaterOrEqualsVersion2() throws Exception {
    String version1 = "3.3.9";
    String version2 = "3.4.*";

    assertThat(version1 + " is not greater than " + version2, isVersionGreaterOrEquals(version1, version2), is(false));
  }

  @Test
  public void isVersionGreaterOrEqualsVersion3() throws Exception {
    String version1 = "3.3.9";
    String version2 = "3.*.*";

    assertThat(version1 + " is not greater than " + version2, isVersionGreaterOrEquals(version1, version2), is(true));
  }

  @Test
  public void isVersionGreaterOrEqualsVersion4() throws Exception {
    String version1 = "3.8";
    String version2 = "3.8.1";

    assertThat(version1 + " is not greater than " + version2, isVersionGreaterOrEquals(version1, version2), is(false));
  }

  @Test
  public void isVersionGreaterOrEqualsVersion5() throws Exception {
    String version1 = "3.8";
    String version2 = "3.8.0";

    assertThat(version1 + " is not greater than " + version2, isVersionGreaterOrEquals(version1, version2), is(true));
  }

  @Test
  public void isVersionGreaterOrEqualsVersion6() throws Exception {
    String version1 = "4.1.3-SNAPSHOT";
    String version2 = "4.1.2";

    assertThat(version1 + " is not greater than " + version2, isVersionGreaterOrEquals(version1, version2), is(true));
  }


  @Test(expected = MigrationException.class)
  public void isVersionValid1() throws Exception {
    String version = "LTFESB-EXERP1-7";
    assertThat(version + "is not valid", isVersionValid(version, MIN_MULE4_VALID_VERSION), is(false));
  }

  @Test(expected = MigrationException.class)
  public void isVersionValid2() throws Exception {
    String version = "A.2.3";
    assertThat(version + "is not valid", isVersionValid(version, MIN_MULE4_VALID_VERSION), is(false));
  }

  @Test
  public void isVersionValid3() throws Exception {
    String version = "4.1";
    assertThat(version + "is not valid", isVersionValid(version, MIN_MULE4_VALID_VERSION), is(false));
  }

  @Test
  public void isVersionValid4() throws Exception {
    String version = "4.1.2";
    assertThat(version + "is not valid", isVersionValid(version, MIN_MULE4_VALID_VERSION), is(true));
  }

  @Test
  public void isVersionValid5() throws Exception {
    String version = "4.1.2-SNAPSHOT";
    assertThat(version + "is not valid", isVersionValid(version, MIN_MULE4_VALID_VERSION), is(true));
  }

  @Test
  public void isVersionValid6() throws Exception {
    String version = "4.1.0-rc.SNAPSHOT";
    assertThat(version + "is not valid", isVersionValid(version, MIN_MULE4_VALID_VERSION), is(true));
  }

  @Test
  public void isVersionValid7() throws Exception {
    String version = "4.1.0-rc-SNAPSHOT";
    assertThat(version + "is not valid", isVersionValid(version, MIN_MULE4_VALID_VERSION), is(true));
  }

}
