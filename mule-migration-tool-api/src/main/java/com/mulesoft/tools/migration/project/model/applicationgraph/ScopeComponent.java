package com.mulesoft.tools.migration.project.model.applicationgraph;

import org.jdom2.Element;

public class ScopeComponent extends MessageProcessor {
    private ApplicationGraph scopedGraph;
    
    public ScopeComponent(Element element, Flow parentFlow, ApplicationGraph parentGraph, ApplicationGraph subGraph) {
        super(element, parentFlow, parentGraph);
        this.scopedGraph = subGraph;
    }
    
    @Override 
    public void accept(FlowComponentVisitor visitor) {
        visitor.visitScopeComponent(this);
    }
}
