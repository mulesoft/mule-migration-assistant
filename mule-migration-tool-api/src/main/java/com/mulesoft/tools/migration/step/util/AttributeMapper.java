/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.step.util;

/**
 * Mapper for attributes
 * 
 * @author Mulesoft Inc.
 * 
 * @param <E> type for the attribute
 */
public interface AttributeMapper<E> {

  /**
   * Gets migrated attribute name
   *
   * @author Mulesoft Inc.
   * 
   * @return migrated attribute name
   */
  E getAttributeName();
}
