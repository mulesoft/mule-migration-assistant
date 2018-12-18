/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.compression;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.util.XmlDslUtils;

import java.util.List;

import org.jdom2.Element;

/**
 * Inlines global compression transformers
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CompressionInlinerStep extends AbstractApplicationModelMigrationStep {

  public CompressionInlinerStep() {
    setAppliedTo(String.format("/*/*[local-name()='%s' or local-name()='%s' and @name and namespace-uri() = '%s']",
                               GZipCompressTransformer.ORIGINAL_ELEMENT_NAME,
                               GZipUncompressTransformer.ORIGINAL_ELEMENT_NAME,
                               XmlDslUtils.CORE_NS_URI));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    String refName = element.getAttribute("name").getValue();
    List<Element> refs = getApplicationModel().getNodes(String.format("//mule:transformer[@ref = '%s']", refName));

    refs.forEach(ref -> {
      Element parent = ref.getParentElement();
      parent.addContent(getIndex(parent, ref), element.clone());
      ref.detach();
    });

    element.detach();
  }

  private int getIndex(Element parent, Element item) {
    int i = 0;
    for (Element child : parent.getChildren()) {
      if (child == item) {
        return i;
      }
      i++;
    }

    throw new RuntimeException("Could not locate transformer ref position");
  }
}
