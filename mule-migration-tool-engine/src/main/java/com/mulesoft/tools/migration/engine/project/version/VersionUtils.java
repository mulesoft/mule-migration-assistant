/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.project.version;

import com.vdurmont.semver4j.Semver;

import static com.vdurmont.semver4j.Semver.SemverType.NPM;
import static de.skuzzle.semantic.Version.create;

/**
 * Obtain a version from a string
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class VersionUtils {

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
