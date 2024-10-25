/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.json;

import static org.jdom2.Namespace.getNamespace;

import org.jdom2.Namespace;

/**
 * Common JSON constants
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public interface JsonMigrationStep {

  String JSON_NAMESPACE_PREFIX = "json";
  String JSON_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/json";
  Namespace JSON_NAMESPACE = getNamespace(JSON_NAMESPACE_PREFIX, JSON_NAMESPACE_URI);

}
