/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.engine.project.structure.util;

import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

/**
 * Sorted properties for deterministic pom output
 *
 * @author Mulesoft Inc.
 */
public class SortedProperties extends Properties {

  private final TreeMap<Object, Object> sortedProperties;

  public SortedProperties(Properties properties) {
    this.putAll(properties);
    sortedProperties = new TreeMap<>(properties);
  }

  @Override
  public Set<Object> keySet() {
    return sortedProperties.keySet();
  }
}
