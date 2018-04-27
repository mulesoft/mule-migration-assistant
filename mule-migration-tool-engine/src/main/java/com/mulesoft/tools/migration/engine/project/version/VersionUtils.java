/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.project.version;

import com.mulesoft.tools.migration.task.Version;
import com.vdurmont.semver4j.Semver;

import static com.vdurmont.semver4j.Semver.SemverType.LOOSE;

/**
 * Obtain a version from a string
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class VersionUtils {

  public static Version buildVersion(String version) {
    Semver semver = new Semver(version, LOOSE);

    return new Version.VersionBuilder()
        .withMajor(semver.getMajor().toString())
        .withMinor(semver.getMinor().toString())
        .withRevision(semver.getPatch().toString())
        .build();
  }
}
