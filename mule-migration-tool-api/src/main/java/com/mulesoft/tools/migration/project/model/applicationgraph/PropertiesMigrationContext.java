/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;

/**
 * Models the context for properties migration
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public class PropertiesMigrationContext {

  private final Map<String, PropertyMigrationContext> inboundContext;
  private final Map<String, PropertyMigrationContext> outboundContext;
  private final SourceType originatingSource;

  public PropertiesMigrationContext(Map<String, PropertyMigrationContext> inboundContext,
                                    Map<String, PropertyMigrationContext> outboundContext,
                                    SourceType originatingSource) {
    this.inboundContext = ImmutableMap.copyOf(inboundContext);
    this.outboundContext = ImmutableMap.copyOf(outboundContext);
    this.originatingSource = originatingSource;
  }

  public Map<String, PropertyMigrationContext> getInboundContext() {
    return this.inboundContext;
  }

  public Map<String, PropertyMigrationContext> getOutboundContext() {
    return this.outboundContext;
  }

  public SourceType getOriginatingSource() {
    return originatingSource;
  }
}
