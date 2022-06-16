/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import org.jdom2.Element;

/**
 * Models a copy-properties mule message processor
 *
 * @author Mulesoft Inc.
 * @since 1.4.0
 */
public class CopyPropertiesProcessor extends AbstractPropertyProcessor {

  public static String OUTBOUND_PREFIX = "outbound_";

  public CopyPropertiesProcessor(Element xmlElement, Flow parentFLow,
                                 ApplicationGraph graph) {
    super(xmlElement, parentFLow, graph);
  }

  @Override
  public void accept(FlowComponentVisitor visitor) {
    visitor.visitCopyPropertiesProcessor(this);
  }

  public String getPropertyTranslation(String propertyKey) {
    return String.format("vars.%s%s", OUTBOUND_PREFIX, propertyKey);
  }
}

