/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.amqp.values;

import com.mulesoft.tools.migration.step.util.AttributeMapper;
import com.mulesoft.tools.migration.step.util.AttributeValueMapper;

/**
 * A Simple attribute mapper.
 * 
 * @author Mulesoft Inc.
 *
 */
public class SimpleAttributeMapper implements AttributeValueMapper<String>, AttributeMapper<String> {

  private String attributeName;

  public SimpleAttributeMapper(String attributeName) {
    this.attributeName = attributeName;
  }

  @Override
  public String getAttributeName() {
    return attributeName;
  }

  @Override
  public String getAttributeValue(String attributeValue) {
    return attributeValue;
  }

}
