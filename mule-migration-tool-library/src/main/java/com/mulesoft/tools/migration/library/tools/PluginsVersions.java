/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
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
      throw new MigrationAbortException("Could not initialize migration assistant", e);
    }
  }

  public static String targetVersion(String pluginName) {
    return INSTANCE.props.getProperty(pluginName);

  }
}
