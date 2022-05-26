/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.nocompatibility;

import com.mulesoft.tools.migration.project.model.applicationgraph.SourceType;

import java.util.Map;
import java.util.Optional;

/**
 * Interface for translating properties given a source type
 *
 * @author Mulesoft Inc.
 * @since 1.4.0
 */
public interface PropertyTranslator {

  Optional<Map<String, String>> getAllTranslationsFor(SourceType sourceType) throws Exception;

  String translateImplicit(String propertyToTranslate, SourceType originatingSourceType);
}
