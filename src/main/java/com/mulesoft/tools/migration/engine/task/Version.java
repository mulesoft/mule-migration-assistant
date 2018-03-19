/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.task;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

/**
 * Defines a version as known by semver
 * 
 * @author Mulesoft Inc.
 */
public class Version {

  private static final String DEFAULT = "*";

  private String major;
  private String minor;
  private String revision;

  private Version(String major, String minor, String revision) {
    this.major = major;
    this.minor = minor;
    this.revision = revision;
  }


  public Boolean matches(Version version) {
    if (this.equals(version)) {
      return TRUE;
    }

    if ((major.equals(DEFAULT) || major.equals(version.getMajor())) &&
        (minor.equals(DEFAULT) || minor.equals(version.getMinor())) &&
        (revision.equals(DEFAULT) || revision.equals(version.getRevision()))) {
      return TRUE;
    }

    return FALSE;
  }

  public String getMajor() {
    return major;
  }

  public String getMinor() {
    return minor;
  }

  public String getRevision() {
    return revision;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Version)) {
      return false;
    }

    Version version = (Version) o;

    if (getMajor() != null ? !getMajor().equals(version.getMajor()) : version.getMajor() != null) {
      return false;
    }
    if (getMinor() != null ? !getMinor().equals(version.getMinor()) : version.getMinor() != null) {
      return false;
    }
    return getRevision() != null ? getRevision().equals(version.getRevision()) : version.getRevision() == null;

  }

  @Override
  public int hashCode() {
    int result = getMajor() != null ? getMajor().hashCode() : 0;
    result = 31 * result + (getMinor() != null ? getMinor().hashCode() : 0);
    result = 31 * result + (getRevision() != null ? getRevision().hashCode() : 0);
    return result;
  }


  /**
   * Builder for {@link Version}
   */
  public static class VersionBuilder {

    public static Version ANY_VERSION = new VersionBuilder().build();

    private String major = DEFAULT;
    private String minor = DEFAULT;
    private String revision = DEFAULT;

    public VersionBuilder withMajor(String major) {
      this.major = major;
      return this;
    }

    public VersionBuilder withMinor(String minor) {
      this.minor = minor;
      return this;
    }

    public VersionBuilder withRevision(String revision) {
      this.revision = revision;
      return this;
    }

    public Version build() {
      checkArgument(isNotBlank(major), "The major value must not be null nor empty");
      checkArgument(DEFAULT.equals(major) || isNumeric(major), "The major value must be * or a number");
      checkArgument(isNotBlank(minor), "The minor value must not be null nor empty");
      checkArgument(DEFAULT.equals(minor) || isNumeric(minor), "The major value must be * or a number");
      checkArgument(isNotBlank(revision), "The revision value must not be null nor empty");
      checkArgument(DEFAULT.equals(revision) || isNumeric(revision), "The major value must be * or a number");
      return new Version(major, minor, revision);
    }
  }
}
