/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.amqp.values;

/**
 * Delivery Mode Attribute Mapper.
 * 
 * @author Mulesoft Inc.
 *
 */
public class DeliveryModeAttributeMapper extends AmqpAttributeMapper {

  public DeliveryModeAttributeMapper() {
    super("deliveryMode");
  }

  @Override
  public void populateValueMapper() {
    valueMapper.put("PERSISTENT", "PERSISTENT");
    valueMapper.put("NON_PERSISTENT", "TRANSIENT");
  }

}
