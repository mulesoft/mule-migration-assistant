/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.tools.mel.nocompatibility;

import com.mulesoft.tools.migration.project.model.applicationgraph.ApplicationGraph;
import com.mulesoft.tools.migration.project.model.applicationgraph.PropertyTranslator;
import com.mulesoft.tools.migration.project.model.applicationgraph.PropertiesMigrationContext;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Resolves mel expressions using inbound properties in no compatibility mode
 *
 * @author Mulesoft Inc.
 * @since 1.4.0
 */
public class InboundPropertiesNoCompatibilityResolver extends PropertiesNoCompatibilityResolver {

  private static final Pattern INBOUND_MAP_PATTERN =
      Pattern.compile("(message\\.inboundProperties[^\\[|\\.]+[$]*)");
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
    super(INBOUND_MAP_PATTERN, GENERAL_INBOUND_PATTERN,
          ImmutableList.of(INBOUND_PATTERN_WITH_BRACKETS, INBOUND_PATTERN_WITH_DOT, INBOUND_PATTERN_WITH_HEADER),
          INBOUND_PATTERN_WITH_EXPRESSION,
          INBOUND_PATTERN_ONLY_EXPRESSION);
  }

  @Override
  protected PropertyTranslator getTranslator(ApplicationGraph graph) {
    return graph.getInboundTranslator();
  }

  @Override
  protected List<String> getPropertyTranslations(PropertiesMigrationContext context, String propertyToTranslate,
                                                 PropertyTranslator translator) {
    return context.getInboundTranslation(propertyToTranslate);
  }

}
