/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import com.mulesoft.tools.migration.project.model.ApplicationModel;

import java.util.List;
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

  void initializeTranslationsForApplicationSourceTypes(ApplicationModel applicationModel);

  Map<SourceType, Map<String, String>> getTranslationsForApplicationsSourceTypes();

  Optional<Map<String, String>> getAllTranslationsFor(SourceType sourceType) throws Exception;

  String translateImplicit(String propertyToTranslate, SourceType sourceType);

  Map<SourceType, String> translateImplicit(String propertyToTranslate, Set<SourceType> originatingSourceTypes);
}
