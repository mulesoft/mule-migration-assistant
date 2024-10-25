/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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
