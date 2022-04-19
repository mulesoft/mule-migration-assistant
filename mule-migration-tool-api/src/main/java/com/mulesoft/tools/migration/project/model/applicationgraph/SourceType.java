/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

/**
 * Models an inbound properties source type
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public interface SourceType {

  public String getNamespaceUri();

  public String getType();

  public boolean supportsImplicit();

  public String getImplicitPrefix();

  public boolean isFlowSource();
}
