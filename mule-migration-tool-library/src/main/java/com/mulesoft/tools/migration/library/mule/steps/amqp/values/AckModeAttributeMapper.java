/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
