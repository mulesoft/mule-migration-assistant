/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
