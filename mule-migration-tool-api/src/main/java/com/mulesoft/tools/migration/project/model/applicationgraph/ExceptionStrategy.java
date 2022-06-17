package com.mulesoft.tools.migration.project.model.applicationgraph;

import org.jdom2.Element;

public class ExceptionStrategy extends ScopeComponent {

    public ExceptionStrategy(Element xmlElement, ApplicationGraph graph, ApplicationGraph subgraph) {
        super(xmlElement, null, graph, subgraph);
    }
    
    
}
