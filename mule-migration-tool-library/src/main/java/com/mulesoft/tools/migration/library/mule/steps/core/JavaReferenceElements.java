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
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Remove elements from 3.x that required references to Java to work
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class JavaReferenceElements extends AbstractApplicationModelMigrationStep {

  // custom-filter already has its own migrator
  // custom-security-provider and custom-encryption-strategy still exist
  public static final String XPATH_SELECTOR = ""
      + "//*["
      + "local-name()='component' or "
      + "local-name()='pooled-component' or "
      + "local-name()='custom-agent' or "
      + "local-name()='custom-queue-store' or "
      + "local-name()='custom-processor' or " // actually deprecated
      + "local-name()='custom-source' or "
      + "local-name()='custom-entry-point-resolver-set' or "
      + "local-name()='custom-entry-point-resolver' or "
      + "local-name()='reconnect-custom-strategy' or "
      + "local-name()='reconnect-custom-notifier' or "
      + "local-name()='custom-service' or "
      + "local-name()='custom-processing-strategy' or "
      + "local-name()='custom-transaction-manager' or "
      + "local-name()='custom-security-filter' or "
      + "local-name()='custom-interceptor' or "
      + "local-name()='custom-transformer' or " // actually deprecated
      + "local-name()='custom-exception-strategy' or "
      + "local-name()='custom-connector' or "
      + "local-name()='custom-object-store' or "
      + "local-name()='custom-aggregator' or "
      + "local-name()='custom-splitter' or "
      + "local-name()='custom-router' or "
      + "local-name()='custom-correlation-aggregator-router' or "
      + "local-name()='custom-inbound-router' or "
      + "local-name()='custom-async-reply-router' or "
      + "local-name()='custom-outbound-router' or "
      + "local-name()='custom-catch-all-strategy' or "
      + "local-name()='custom-forwarding-catch-all-strategy' or "
      + "local-name()='custom-message-info-mapping' or "
      + "local-name()='custom-lifecycle-adapter-factory'"
      + "]";

  @Override
  public String getDescription() {
    return "Remove elements from 3.x that required referecces to Java to work";
  }

  public JavaReferenceElements() {
    this.setAppliedTo(XPATH_SELECTOR);
  }


  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    report.report("components.java", object, object, object.getName());
  }

}
