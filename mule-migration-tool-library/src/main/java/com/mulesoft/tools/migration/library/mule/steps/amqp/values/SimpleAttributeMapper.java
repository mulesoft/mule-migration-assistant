/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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
