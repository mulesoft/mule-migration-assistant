/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import com.mulesoft.tools.migration.project.model.ApplicationModel;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Interface for translating properties given a source type
 *
 * @author Mulesoft Inc.
 * @since 1.4.0
 */
public interface PropertyTranslator {

  String OUTBOUND_PREFIX = "outbound_";
  String VARS_OUTBOUND_PREFIX = "vars." + OUTBOUND_PREFIX;
  String NO_COMPATIBILITY_OUTBOUND_MAP_EXPRESSION =
      "vars filterObject ($$ startsWith 'outbound_') mapObject {($$ dw::core::Strings::substringAfter '_'): $}";

  static String outboundVariableExpression(String property) {
    if (property.contains(".")) {
      return String.format("vars['%s%s']", OUTBOUND_PREFIX, property);
    } else {
      return VARS_OUTBOUND_PREFIX + property;
    }
  }

  void initializeTranslationsForApplicationSourceTypes(ApplicationModel applicationModel);

  Map<SourceType, Map<String, String>> getTranslationsForApplicationsSourceTypes();

  Map<String, String> getAllTranslationsFor(SourceType sourceType);

  String translateImplicit(String propertyToTranslate, SourceType sourceType);

  Map<SourceType, String> translateImplicit(String propertyToTranslate, Set<SourceType> originatingSourceTypes);
}
