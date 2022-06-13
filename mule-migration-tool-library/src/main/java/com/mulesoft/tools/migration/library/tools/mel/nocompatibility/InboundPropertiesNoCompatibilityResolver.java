/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.tools.mel.nocompatibility;

import com.google.common.collect.ImmutableList;
import com.mulesoft.tools.migration.project.model.applicationgraph.ApplicationGraph;
import com.mulesoft.tools.migration.project.model.applicationgraph.PropertyTranslator;
import com.mulesoft.tools.migration.project.model.applicationgraph.PropertiesMigrationContext;
import com.mulesoft.tools.migration.project.model.applicationgraph.PropertyMigrationContext;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Resolves mel expressions using inbound properties in no compatibility mode
 *
 * @author Mulesoft Inc.
 * @since 1.4.0
 */
public class InboundPropertiesNoCompatibilityResolver extends PropertiesNoCompatibilityResolver {

  private static final Pattern GENERAL_INBOUND_PATTERN =
      Pattern.compile("(message\\.inboundProperties(?:\\.'?[\\.a-zA-Z0-9]*'?|\\['?.*'+?\\]))");
  private static final Pattern INBOUND_PATTERN_WITH_BRACKETS =
      Pattern.compile("message\\.inboundProperties\\['(.*?)'\\]");
  private static final Pattern INBOUND_PATTERN_WITH_DOT =
      Pattern.compile("message\\.inboundProperties\\.'?(.*?)'?");
  private static final Pattern INBOUND_PATTERN_WITH_HEADER = Pattern.compile("(?i)^header:inbound:");
  private static final Pattern INBOUND_PATTERN_WITH_EXPRESSION =
      Pattern.compile("message\\.inboundProperties\\[\'(.*)\'\\]");
  private static final Pattern INBOUND_PATTERN_ONLY_EXPRESSION =
      Pattern.compile("message\\.inboundProperties\\[[^'].*\\]");

  public InboundPropertiesNoCompatibilityResolver() {
    super(GENERAL_INBOUND_PATTERN,
          ImmutableList.of(INBOUND_PATTERN_WITH_BRACKETS, INBOUND_PATTERN_WITH_DOT, INBOUND_PATTERN_WITH_HEADER),
          INBOUND_PATTERN_WITH_EXPRESSION,
          INBOUND_PATTERN_ONLY_EXPRESSION);
  }

  @Override
  public Map<String, PropertyMigrationContext> getPropertiesContextMap(
                                                                       PropertiesMigrationContext propertiesMigrationContext) {
    return propertiesMigrationContext.getInboundContext();
  }

  @Override
  protected PropertyTranslator getTranslator(ApplicationGraph graph) {
    return graph.getInboundTranslator();
  }

  @Override
  protected String getPropertyTranslation(PropertiesMigrationContext context, String propertyToTranslate,
                                          PropertyTranslator translator) {
    return context.getInboundTranslation(propertyToTranslate, translator, true);
  }


}
