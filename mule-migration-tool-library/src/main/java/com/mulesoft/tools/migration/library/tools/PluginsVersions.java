/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.tools;

import com.mulesoft.tools.migration.exception.MigrationAbortException;

import java.io.IOException;
import java.util.Properties;

/**
 * Provides access to the externalized plugin versions configuration.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class PluginsVersions {

  private static final PluginsVersions INSTANCE = new PluginsVersions();

  private final Properties props;

  private PluginsVersions() {
    try {
      props = new Properties();
      props.load(PluginsVersions.class.getResourceAsStream("/target-versions.properties"));
    } catch (IOException e) {
      throw new MigrationAbortException("Could not initialize migration tool", e);
    }
  }

  public static String targetVersion(String pluginName) {
    return INSTANCE.props.getProperty(pluginName);

  }
}
