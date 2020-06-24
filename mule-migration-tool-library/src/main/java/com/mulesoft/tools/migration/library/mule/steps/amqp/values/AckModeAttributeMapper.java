/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.amqp.values;


/**
 * Ack mode attribute mapper.
 * 
 * @author Mulesoft Inc.
 *
 */
public class AckModeAttributeMapper extends AmqpAttributeMapper {

  public AckModeAttributeMapper() {
    super("ackMode");
  }

  @Override
  public void populateValueMapper() {
    valueMapper.put("AMQP_AUTO", "IMMEDIATE");
    valueMapper.put("MULE_AUTO", "AUTO");
    valueMapper.put("MANUAL", "MANUAL");
  }

}
