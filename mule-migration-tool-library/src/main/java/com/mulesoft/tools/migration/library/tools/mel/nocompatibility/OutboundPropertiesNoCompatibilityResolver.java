/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.tools.mel.nocompatibility;

import com.mulesoft.tools.migration.library.nocompatibility.PropertyTranslator;
import com.mulesoft.tools.migration.project.model.applicationgraph.PropertiesMigrationContext;
import com.mulesoft.tools.migration.project.model.applicationgraph.PropertyMigrationContext;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Resolves mel expressions using outbound properties in no compatibility mode
 *
 * @author Mulesoft Inc.
 * @since 1.4.0
 */
public class OutboundPropertiesNoCompatibilityResolver extends PropertiesNoCompatibilityResolver {

  private static final Pattern GENERAL_INBOUND_PATTERN =
      Pattern.compile("(message\\.outboundProperties(?:\\.'?[\\.a-zA-Z0-9]*'?|\\['?.*'+?\\]))");
  private static final Pattern INBOUND_PATTERN_WITH_BRACKETS =
      Pattern.compile("message\\.outboundProperties\\['(.*?)'\\]");
  private static final Pattern INBOUND_PATTERN_WITH_DOT =
      Pattern.compile("message\\.outboundProperties\\.'?(.*?)'?");
  private static final Pattern INBOUND_PATTERN_WITH_EXPRESSION =
      Pattern.compile("message\\.outboundProperties\\[\'(.*)\'\\]");
  private static final Pattern INBOUND_PATTERN_ONLY_EXPRESSION =
      Pattern.compile("message\\.outboundProperties\\[[^'].*\\]");

  public OutboundPropertiesNoCompatibilityResolver() {
    super(GENERAL_INBOUND_PATTERN, INBOUND_PATTERN_WITH_BRACKETS, INBOUND_PATTERN_WITH_DOT, INBOUND_PATTERN_WITH_EXPRESSION,
          INBOUND_PATTERN_ONLY_EXPRESSION);
  }

  @Override
  public Map<String, PropertyMigrationContext> getPropertiesContextMap(
                                                                       PropertiesMigrationContext propertiesMigrationContext) {
    return propertiesMigrationContext.getOutboundContext();
  }

  @Override
  protected PropertyTranslator getTranslator() {
    return null;
  }

}
