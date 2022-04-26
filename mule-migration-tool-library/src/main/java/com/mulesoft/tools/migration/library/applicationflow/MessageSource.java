package com.mulesoft.tools.migration.library.applicationflow;

import org.jdom2.Element;

public class MessageSource implements PropertiesSource, FlowComponent {
  private final Element elementXml;
  private final String type;
  private final Flow parentFlow;

  public MessageSource(Element elementXml, Flow parentFlow) {
    this.elementXml = elementXml;
    this.type = elementXml.getName();
    this.parentFlow = parentFlow;
  }

  public Element getXmlElement() {
    return this.elementXml;
  }

  @Override 
  public String getType() {
    return type;
  }

  @Override 
  public Flow getParentFlow() {
    return parentFlow;
  }
}
