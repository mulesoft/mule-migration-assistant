/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.step.util;

/**
 * Mapper for attribute values
 * 
 * @author Mulesoft Inc.
 *
 * @param <E> type for the attribute
 */
@FunctionalInterface
public interface AttributeValueMapper<E> {

  /**
   * Retrieves an attribute migrated value
   * 
   * @author Mulesoft Inc.
   * 
   * @param attributeValue original value
   * @return migrated value
   */
  E getAttributeValue(E attributeValue);
}
