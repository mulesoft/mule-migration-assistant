/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.tools.mel.nocompatibility;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mulesoft.tools.migration.project.model.applicationgraph.ApplicationGraph;
import com.mulesoft.tools.migration.project.model.applicationgraph.PropertyTranslator;
import com.mulesoft.tools.migration.project.model.applicationgraph.PropertiesMigrationContext;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Resolves mel expressions using outbound properties in no compatibility mode
 *
 * @author Mulesoft Inc.
 * @since 1.4.0
 */
public class OutboundPropertiesNoCompatibilityResolver extends PropertiesNoCompatibilityResolver {

  private static final Pattern OUTBOUND_MAP_PATTERN =
      Pattern.compile(".*message\\.outboundProperties(?!(?:\\[|\\.))+.*$");
  private static final Pattern GENERAL_OUTBOUND_PATTERN =
      Pattern.compile("(message\\.outboundProperties(?:\\.'?[\\.a-zA-Z0-9]*'?|\\['?.*'+?\\]))");
  private static final Pattern OUTBOUND_PATTERN_WITH_BRACKETS =
      Pattern.compile("message\\.outboundProperties\\['(.*?)'\\]");
  private static final Pattern OUTBOUND_PATTERN_WITH_DOT =
      Pattern.compile("message\\.outboundProperties\\.'?(.*?)'?");
  private static final Pattern OUTBOUND_PATTERN_WITH_HEADER = Pattern.compile("(?i)^(header:outbound:|header:)");
  private static final Pattern OUTBOUND_PATTERN_WITH_EXPRESSION =
      Pattern.compile("message\\.outboundProperties\\[\'(.*)\'\\]");
  private static final Pattern OUTBOUND_PATTERN_ONLY_EXPRESSION =
      Pattern.compile("message\\.outboundProperties\\[[^'].*\\]");

  public OutboundPropertiesNoCompatibilityResolver() {
    super(OUTBOUND_MAP_PATTERN, GENERAL_OUTBOUND_PATTERN,
          ImmutableList.of(OUTBOUND_PATTERN_WITH_BRACKETS, OUTBOUND_PATTERN_WITH_DOT, OUTBOUND_PATTERN_WITH_HEADER),
          OUTBOUND_PATTERN_WITH_EXPRESSION,
          OUTBOUND_PATTERN_ONLY_EXPRESSION);
  }

  @Override
  protected PropertyTranslator getTranslator(ApplicationGraph graph) {
    return null;
  }

  @Override
  protected List<String> getPropertyTranslations(PropertiesMigrationContext context, String propertyToTranslate,
                                                 PropertyTranslator translator) {
    return context.getOutboundTranslation(propertyToTranslate, true);
  }

}
