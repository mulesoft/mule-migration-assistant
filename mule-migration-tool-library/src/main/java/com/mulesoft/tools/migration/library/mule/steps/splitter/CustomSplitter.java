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
package com.mulesoft.tools.migration.library.mule.steps.splitter;

import static com.mulesoft.tools.migration.library.mule.steps.splitter.CollectionSplitter.COLLECTION_AGGREGATOR;
import static com.mulesoft.tools.migration.library.mule.steps.splitter.SplitterAggregatorUtils.setAggregatorAsProcessed;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NS_URI;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Handles migration for 'custom-splitter' and matching aggregator.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CustomSplitter extends AbstractSplitter {

  private static final String XPATH_SELECTOR = "//*[local-name()='custom-splitter' and namespace-uri()='" + CORE_NS_URI + "']";

  public CustomSplitter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element splitter, MigrationReport report) throws RuntimeException {
    report.report("splitter.custom", splitter, splitter);
    getMatchingAggregatorElement(splitter).ifPresent(
                                                     a -> {
                                                       setAggregatorAsProcessed(a);
                                                       report.report("aggregator.customSplitter", a, a);
                                                     });
  }

  @Override
  protected String getMatchingAggregatorName() {
    return COLLECTION_AGGREGATOR;
  }
}
