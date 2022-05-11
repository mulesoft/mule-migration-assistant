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
  private String originalProperty;
  private String translation;

  public PropertyMigrationContext(String translation) {
    this.translation = translation;
  }

  public PropertyMigrationContext(String translation, boolean optional) {
    this.translation = translation;
    this.optional = optional;
  }

  public String getOriginalProperty() {
    return originalProperty;
  }

  public boolean isOptional() {
    return optional;
  }

  public String getTranslation() {
    return translation;
  }
}
