/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.batch;

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
  private static final Namespace CORE_NAMESPACE = Namespace.getNamespace("core", "http://www.mulesoft.org/schema/mule/core");
  public static final String XPATH_SELECTOR = "/mule:mule/batch:job";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update batch job to a flow with equal name.";
  }

  public BatchJob() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element originalBatchJob, MigrationReport report) throws RuntimeException {
    Element batchJob = new Element("job", BATCH_NAMESPACE);
    setAttributes(originalBatchJob, batchJob);

    Optional<Element> batchInput = Optional.ofNullable(originalBatchJob.getChild("input", BATCH_NAMESPACE));
    batchInput.ifPresent(input -> originalBatchJob.removeContent(input));

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
