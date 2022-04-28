package com.mulesoft.tools.migration.library.applicationflow;

import com.mulesoft.tools.migration.library.mule.steps.nocompatibility.InboundToAttributesTranslator;
import org.jdom2.Element;

public class MessageSource implements PropertiesSource, FlowComponent {

  private final Element elementXml;
  private final InboundToAttributesTranslator.SourceType type;
  private final Flow parentFlow;

  public MessageSource(Element xmlElement, Flow parentFlow) {
    this.elementXml = xmlElement;
    this.type = new InboundToAttributesTranslator.SourceType(xmlElement.getNamespaceURI(), xmlElement.getName());
    this.parentFlow = parentFlow;
  }

  public Element getXmlElement() {
    return this.elementXml;
  }

  @Override
  public InboundToAttributesTranslator.SourceType getType() {
    return type;
  }

  @Override
  public Flow getParentFlow() {
    return parentFlow;
  }
}
