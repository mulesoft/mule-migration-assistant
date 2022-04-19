package com.mulesoft.tools.migration.library.applicationflow;

import com.google.common.collect.Maps;
import org.jdom2.Element;

import java.util.Map;

public class MessageSource {

  private final Element elementXml;
  private String type;

  public Map<String, String> getPropertiesContext() {
    return Maps.newHashMap();
  }

  public MessageSource(Element elementXml) {
    this.elementXml = elementXml;
    this.type = elementXml.getNamespacePrefix() + ":" + elementXml.getName();
  }

  public String getType() {
    return this.type;
  }

  public Element getXmlElement() {
    return this.elementXml;
  }
}
