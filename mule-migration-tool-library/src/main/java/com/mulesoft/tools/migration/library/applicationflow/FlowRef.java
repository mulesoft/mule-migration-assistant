package com.mulesoft.tools.migration.library.applicationflow;

import org.jdom2.Element;

public class FlowRef extends MessageProcessor {
    private Flow source;
    private Flow destination;

    public FlowRef(Element xmlElement, Flow source, Flow destination) {
        super(xmlElement, source);
        this.source = source;
        this.destination = destination;
    }

    public FlowRef(Element xmlElement, Flow source) {
        super(xmlElement, source);
        this.source = source;
    }

    public Flow getDestinationFlow() {
        return destination;
    }
}
