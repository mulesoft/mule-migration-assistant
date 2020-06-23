/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
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
    String version = "4.1.2-rc.SNAPSHOT";
    assertThat(version + "is not valid", isVersionValid(version, MIN_MULE4_VALID_VERSION), is(true));
  }

  @Test
  public void isVersionValid7() throws Exception {
    String version = "4.1.1-rc-SNAPSHOT";
    assertThat(version + "is not valid", isVersionValid(version, MIN_MULE4_VALID_VERSION), is(true));
  }

}
