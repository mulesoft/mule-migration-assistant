/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.applicationflow;

import org.jdom2.Element;

import java.util.List;

/**
 * Models a mule flow
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public class Flow {

  private String name;
  private Element xmlElement;
  private List<FlowComponent> flowComponents;

  public Flow(Element xmlElement) {
    this.xmlElement = xmlElement;
    this.name = xmlElement.getAttribute("name").getValue();
  }

  public Element getXmlElement() {
    return xmlElement;
  }

  public String getName() {
    return name;
  }

  public void setComponents(List<FlowComponent> flowComponents) {
    this.flowComponents = flowComponents;
  }
}
