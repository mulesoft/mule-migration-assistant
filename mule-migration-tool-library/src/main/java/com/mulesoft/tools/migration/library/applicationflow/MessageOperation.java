/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.applicationflow;

import com.mulesoft.tools.migration.library.mule.steps.nocompatibility.InboundToAttributesTranslator;
import org.jdom2.Element;

/**
 * Special type of Message processor that is a PropertySource 
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
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
