/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

/**
 * Models the context for doing property migration
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public class PropertyMigrationContext {

  private boolean optional = false;
  private boolean removeNext = false;
  private String translation;

  public PropertyMigrationContext(String translation) {
    this.translation = translation;
  }

  public PropertyMigrationContext(String translation, boolean optional, boolean removeNext) {
    this.translation = translation;
    this.optional = optional;
    this.removeNext = removeNext;
  }

  public boolean isOptional() {
    return optional;
  }

  public boolean isRemoveNext() {
    return removeNext;
  }

  public PropertyMigrationContext setRemoveNext() {
    return new PropertyMigrationContext(this.translation, this.optional, true);
  }

  public String getTranslation() {
    return translation;
  }
}
