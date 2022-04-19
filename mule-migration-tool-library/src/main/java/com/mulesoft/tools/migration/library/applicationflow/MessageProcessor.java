package com.mulesoft.tools.migration.library.applicationflow;

import org.jdom2.Element;

public class MessageProcessor {

  private Element xmlElement;
  private Flow parentFLow;

  public MessageProcessor(Element xmlElement, Flow parentFLow) {
    this.xmlElement = xmlElement;
    this.parentFLow = parentFLow;
  }

  public Element getXmlElement() {
    return xmlElement;
  }
}
