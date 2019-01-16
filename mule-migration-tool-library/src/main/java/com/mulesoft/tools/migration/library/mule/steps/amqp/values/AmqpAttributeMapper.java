/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
