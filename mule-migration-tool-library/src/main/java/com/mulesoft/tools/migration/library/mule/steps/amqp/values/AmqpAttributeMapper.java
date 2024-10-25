/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.amqp.values;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps attributes for the amqp connector.
 * 
 * @author Mulesoft Inc.
 */
public abstract class AmqpAttributeMapper extends SimpleAttributeMapper {

  protected Map<String, String> valueMapper = new HashMap<String, String>();

  public AmqpAttributeMapper(String attributeName) {
    super(attributeName);
    populateValueMapper();
  };

  public abstract void populateValueMapper();

  @Override
  public String getAttributeValue(String attributeValue) {
    return valueMapper.get(attributeValue);
  }
}
