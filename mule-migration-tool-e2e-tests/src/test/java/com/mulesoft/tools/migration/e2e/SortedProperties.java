/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.e2e;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SortedProperties extends Properties {

  private static final long serialVersionUID = 1L;

  @Override
  public Set<Map.Entry<Object, Object>> entrySet() {
    final Stream<SimpleEntry<Object, Object>> stream =
        keySet().stream().map(k -> new AbstractMap.SimpleEntry<>(k, getProperty(k.toString())));
    return stream.collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public synchronized Enumeration<Object> keys() {
    return Collections.enumeration(new ArrayList<>(keySet()));
  }

  @Override
  public Set<Object> keySet() {
    return new TreeSet<>(super.keySet());
  }

}
