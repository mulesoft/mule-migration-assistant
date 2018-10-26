/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.step.category;

import com.mulesoft.tools.migration.project.ProjectType;

import org.jdom2.Element;

import java.util.List;

/**
 * An interface to feed the migration report.
 *
 * @param <T>
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public interface MigrationReport<T> {

  /**
   * Represents the severity of a report entry.
   */
  public enum Level {
    /**
     * Represents a migration step that has to be done manually by the app developer. The application may not start-up or work as
     * expected until this item is assessed.
     */
    ERROR,

    /**
     * Represents a migration step that has to be done manually by the app developer, but the application would work as intended
     * even if this item is not assessed.
     */
    WARN,

    /**
     * Represents a change in Mule 4 that will not affect the app behavior. Used when something from the original app had to be
     * removed/changed because it is no longer needed.
     * <p>
     * This is just information to better understand the new features of Mule 4.
     */
    INFO

  }

  /**
   * Sets some basic metadata about the project to be populated into the report.
   *
   * @param projectType
   * @param projectName
   */
  void initialize(ProjectType projectType, String projectName);

  /**
   * Adds the report entry to the target report, and as a comment on the element being processed.
   *
   * @param entryKey the key of the report entry to generate, as defined in a {@code report.yaml} file.
   * @param element the XML element that generated this report entry.
   * @param elementToComment the XML element inside of which the report entry will be.
   * @param messageParams the strings to use to replace value placeholders in report entry message.
   */
  void report(String entryKey, Element element, Element elementToComment, String... messageParams);

  /**
   * Adds the report entry to the target report, and as a comment on the element being processed.
   *
   * @param level
   * @param element the XML element that generated this report entry.
   * @param elementToComment the XML element inside of which the report entry will be.
   * @param message
   * @param documentationLinks the links to the Mule documentation that contain the explanation of any changes needed in the
   *        migrated app.
   *
   * @deprecated Use {@link #report(String, Element, Element)} instead.
   */
  @Deprecated
  void report(Level level, Element element, Element elementToComment, String message, String... documentationLinks);

  /**
   * Adds the passed value to the counter of processed elements.
   * <p>
   * This value is later used as the denominator to calculate the migration ratio.
   *
   * @param processedElements
   */
  void addProcessedElements(int processedElements);

  /**
   * Returns the report entries.
   */
  List<T> getReportEntries();
}
