/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.tools.mel.nocompatibility;

/**
 * Contains the result after the resolver was applied
 *
 * @author Mulesoft Inc.
 * @since 1.4.0
 */
public class NoCompatibilityResolverResult {

  private String translation;
  private boolean successful;

  public NoCompatibilityResolverResult(String translation, boolean successful) {
    this.translation = translation;
    this.successful = successful;
  }

  public boolean isSuccesful() {
    return successful;
  }

  public String getTranslation() {
    return translation;
  }
}
