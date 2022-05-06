/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import java.util.Objects;

/**
 * Models an inbound properties source type
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public class SourceType {

  private String namespaceUri;
  private String type;

  public SourceType(String namespaceUri, String type) {
    this.namespaceUri = namespaceUri;
    this.type = type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    SourceType that = (SourceType) o;
    return Objects.equals(namespaceUri, that.namespaceUri) && Objects.equals(type, that.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(namespaceUri, type);
  }
}
