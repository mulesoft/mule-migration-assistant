package com.mulesoft.tools.migration.library.applicationflow;

import com.mulesoft.tools.migration.library.mule.steps.nocompatibility.InboundToAttributesTranslator;
import org.jdom2.Element;

public class MessageOperation extends MessageProcessor implements PropertiesSource {

  private final InboundToAttributesTranslator.SourceType type;

  public MessageOperation(Element xmlElement, Flow parentFLow) {
    super(xmlElement, parentFLow);
    this.type = new InboundToAttributesTranslator.SourceType(xmlElement.getNamespaceURI(), xmlElement.getName());
  }

  @Override
  public InboundToAttributesTranslator.SourceType getType() {
    return type;
  }
}
