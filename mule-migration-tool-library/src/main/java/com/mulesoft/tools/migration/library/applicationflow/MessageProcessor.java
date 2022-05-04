/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.applicationflow;

import org.jdom2.Element;

/**
 * Models a mule message processor
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public class MessageProcessor implements FlowComponent {

  private Element xmlElement;
  private Flow parentFLow;

  public MessageProcessor(Element xmlElement, Flow parentFLow) {
    this.xmlElement = xmlElement;
    this.parentFLow = parentFLow;
  }

  public Element getXmlElement() {
    return xmlElement;
  }

  @Override
  public Flow getParentFlow() {
    return parentFLow;
  }
}
