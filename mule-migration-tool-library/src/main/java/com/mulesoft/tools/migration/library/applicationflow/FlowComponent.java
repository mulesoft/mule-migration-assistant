package com.mulesoft.tools.migration.library.applicationflow;

import org.jdom2.Element;

public interface FlowComponent {

  Flow getParentFlow();

  Element getXmlElement();
}
