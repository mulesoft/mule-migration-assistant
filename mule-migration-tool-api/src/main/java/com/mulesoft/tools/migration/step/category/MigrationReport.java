/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.step.category;

import org.jdom2.Element;

/**
 * An interface to feed the migration report
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public interface MigrationReport {

  /**
   * Represents the severity of a report entry.
   */
  public enum Level {
    INFO, WARN, ERROR
  }

  /**
   * Adds the report entry to the target report, and as a comment on the element being processed.
   *
   * @param level
   * @param element the XML element that generated this report entry.
   * @param elementToComment the XML element inside of which the report entry will be.
   * @param message
   * @param documentationLinks the links to the Mule documentation that contain the explanation of any changes needed in the
   *        migrated app.
   */
  void report(Level level, Element element, Element elementToComment, String message, String... documentationLinks);
}
