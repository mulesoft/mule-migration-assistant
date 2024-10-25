/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import java.util.List;
import java.util.Objects;

import org.jdom2.Element;

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

  public List<FlowComponent> getComponents() {
    return flowComponents;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Flow flow = (Flow) o;
    return name.equals(flow.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String toString() {
    return name;
  }
}
