/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
