package com.mulesoft.tools.migration.library.applicationflow;

import org.jdom2.Element;

public class MessageOperation extends MessageProcessor implements PropertiesSource {
    private final String type;

    public MessageOperation(Element xmlElement, Flow parentFLow) {
        super(xmlElement, parentFLow);
        this.type = xmlElement.getName();
    }
    
    @Override public String getType() {
        return type;
    }
}
