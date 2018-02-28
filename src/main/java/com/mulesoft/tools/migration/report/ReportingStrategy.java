/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.report;

import com.mulesoft.tools.migration.engine.MigrationTask;
import com.mulesoft.tools.migration.engine.MigrationStep;

/**
 * Defines a generic way of reporting information
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public interface ReportingStrategy {

  /**
   * Report data to the reporting framework
   *
   * @param message
   * @param reportCategory
   * @param filePath
   * @param task
   * @param step
   */
  // TODO change this method name
  void log(String message, ReportCategory reportCategory, String filePath, MigrationTask task, MigrationStep step);
}
