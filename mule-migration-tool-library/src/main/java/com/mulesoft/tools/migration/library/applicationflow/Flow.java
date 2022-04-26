package com.mulesoft.tools.migration.library.applicationflow;

import org.jdom2.Element;

import java.util.List;

public class Flow {

  private String name;
  private Element xmlElement;
  private MessageSource messageSource;
  private List<MessageProcessor> processors;

  public Flow(Element xmlElement) {
    this.xmlElement = xmlElement;
    this.name = xmlElement.getAttribute("name").getValue();
    this.messageSource = messageSource;
  }

  public Element getXmlElement() {
    return xmlElement;
  }

  public boolean hasSource() {
    return messageSource != null;
  }

  public MessageSource getMessageSource() {
    return this.messageSource;
  }
  
  public String getName() {
    return name;
  }
}
