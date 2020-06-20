/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
