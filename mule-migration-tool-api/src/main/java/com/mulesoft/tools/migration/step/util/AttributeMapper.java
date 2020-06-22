/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
