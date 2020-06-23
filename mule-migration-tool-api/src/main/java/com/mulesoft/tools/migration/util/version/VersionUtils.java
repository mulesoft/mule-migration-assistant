/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.util.version;

import com.mulesoft.tools.migration.exception.MigrationException;
import com.vdurmont.semver4j.Semver;

import static com.google.common.base.Preconditions.checkState;
import static com.vdurmont.semver4j.Semver.SemverType.NPM;
import static de.skuzzle.semantic.Version.create;
import static de.skuzzle.semantic.Version.isValidVersion;

/**
 * Obtain a version from a string
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class VersionUtils {

  public static final String MIN_MULE4_VALID_VERSION = "4.1.1";

  /**
   * Validates if a mule 3 version complies with semantic versioning specification
   *
   * @param version the version to validate
   * @return false if the version does not comply with semantic versioning, true otherwise
   */
  public static Boolean isVersionValid(String version, String complyVersion) throws Exception {
    try {
      checkState(version != null, "The input version must not be null");
      return isVersionGreaterOrEquals(version, complyVersion) && isValidVersion(version);
    } catch (Exception e) {
      throw new MigrationException("Failed to continue executing migration: " + e.getMessage(), e);
    }
  }

  /**
   * Validates if {@code version1} is greater or equal than {@code version2}
   *
   * @param version1
   * @param version2
   * @return false if version1 is lesser than version2
   */
  public static Boolean isVersionGreaterOrEquals(String version1, String version2) {
    String v1 = completeIncremental(version1);
    Semver semver = new Semver(v1, NPM);
    return semver.satisfies(version2) || semver.isGreaterThan(version2);
  }

  /**
   * It completes the incremental version number with 0 in the event the version provided has the form x to become x.0.0 or x.y to
   * become x.y.0
   *
   * @param version the version to be completed
   * @return The completed version x.y.z with no qualifier
   */
  public static String completeIncremental(String version) {
    Semver semver = new Semver(version, NPM);
    Integer minor = semver.getMinor();
    Integer patch = semver.getPatch();
    if (minor == null || patch == null) {
      version = create(semver.getMajor(), minor == null ? 0 : minor, patch == null ? 0 : patch).toString();
    }
    return getBaseVersion(version);
  }

  /**
   * Returns the base version, i.e., in the format major.minor.patch.
   *
   * @param version
   * @return the base part of the version.
   */
  public static String getBaseVersion(String version) {
    return new Semver(version).withClearedSuffixAndBuild().getValue();
  }
}
