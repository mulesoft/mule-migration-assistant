/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.util;

import com.mulesoft.tools.migration.task.Version;

/**
 * Versions for Mule Projects
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MuleVersion {

  public static final Version MULE_3_VERSION = new Version.VersionBuilder().withMajor("3").build();
  public static final Version MULE_4_VERSION = new Version.VersionBuilder().withMajor("4").build();
}
