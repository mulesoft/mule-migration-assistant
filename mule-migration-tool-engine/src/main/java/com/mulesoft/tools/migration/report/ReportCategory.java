/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.report;

/**
 * Defines a reporting event
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
// TODO change this name to something like reportr event or something like that
public enum ReportCategory {
  // TODO WTF!!!!! ->>\033
  RULE_APPLIED("\033[32mRULE APPLIED\033[0m"), WORKING_WITH_NODES("\033[36mWORKING WITH NODE\033[0m"), ERROR(
      "\033[31mERROR\033[0m"), WORKING_WITH_FILE("\033[35mWORKING WITH FILE\033[0m"), SKIPPED(
          "\033[37mSKIPPED\033[0m"), TRYING_TO_APPLY("\033[33mTRYING TO APPLY\033[0m");

  private final String description;

  ReportCategory(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
