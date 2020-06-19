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
package com.mulesoft.tools.migration.library.mule.steps.batch;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Migrate BatchJob component
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class BatchJob extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  public static final String BATCH_NAMESPACE_PREFIX = "batch";
  public static final String BATCH_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/batch";
  private static final Namespace BATCH_NAMESPACE = Namespace.getNamespace(BATCH_NAMESPACE_PREFIX, BATCH_NAMESPACE_URI);
  public static final String XPATH_SELECTOR = "/*/*[namespace-uri()='" + BATCH_NAMESPACE_URI + "' and local-name()='job']";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update batch job to a flow with equal name that contains the actual batch job.";
  }

  public BatchJob() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(BATCH_NAMESPACE));
  }

  @Override
  public void execute(Element originalBatchJob, MigrationReport report) throws RuntimeException {
    Element batchJob = new Element("job", BATCH_NAMESPACE);
    setAttributes(originalBatchJob, batchJob);

    Optional<Element> batchInput = Optional.ofNullable(originalBatchJob.getChild("input", BATCH_NAMESPACE));
    batchInput.ifPresent(input -> originalBatchJob.removeContent(input));
    Optional<Element> batchThreadingProfile =
        Optional.ofNullable(originalBatchJob.getChild("threading-profile", BATCH_NAMESPACE));

    batchThreadingProfile.ifPresent(threadingProfile -> {
      originalBatchJob.removeContent(threadingProfile);
      String maxThreadsActive = threadingProfile.getAttributeValue("maxThreadsActive");
      if (maxThreadsActive != null) {
        batchJob.setAttribute("maxConcurrency", maxThreadsActive);
      }
      report.report("flow.threading", originalBatchJob, originalBatchJob);
    });

    List<Element> children = new ArrayList<>(originalBatchJob.getChildren());
    children.forEach(child -> {
      originalBatchJob.removeContent(child);
      batchJob.addContent(child);
    });

    batchInput.ifPresent(input -> {
      List<Element> inputChildren = new ArrayList<>(input.getChildren());
      inputChildren.forEach(child -> {
        input.removeContent(child);
        originalBatchJob.addContent(child);
      });
    });

    originalBatchJob.addContent(batchJob);
    originalBatchJob.setNamespace(CORE_NAMESPACE);
    originalBatchJob.setName("flow");
  }

  private void moveAttribute(Element originalBatchJob, Element batchJob, String oldName, String newName, boolean expression) {
    Optional.ofNullable(originalBatchJob.getAttributeValue(oldName)).ifPresent(value -> {
      originalBatchJob.removeAttribute(oldName);
      batchJob.setAttribute(newName, expression ? expressionMigrator.migrateExpression(value, true, originalBatchJob) : value);
    });
  }

  private void setAttributes(Element originalBatchJob, Element batchJob) {
    batchJob.setAttribute("jobName", originalBatchJob.getAttributeValue("name"));
    moveAttribute(originalBatchJob, batchJob, "scheduling-strategy", "schedulingStrategy", false);
    moveAttribute(originalBatchJob, batchJob, "max-failed-records", "maxFailedRecords", false);
    moveAttribute(originalBatchJob, batchJob, "block-size", "blockSize", false);
    moveAttribute(originalBatchJob, batchJob, "job-instance-id", "jobInstanceId", true);
  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }
}
