/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

/**
 * Models a type of component that emits inbound properties
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public interface PropertiesSource {

  SourceType getType();
}
