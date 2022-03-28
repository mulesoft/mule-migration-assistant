/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.step.category;

import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.pom.PomModel;

import org.jdom2.Element;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

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
   * @param projectType a {@link ProjectType}
   * @param projectName the project name
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
   * @deprecated Use {@link #report(String, Element, Element, String...)} instead.
   */
  @Deprecated
  void report(Level level, Element element, Element elementToComment, String message, String... documentationLinks);

  /**
   * Adds the passed value to the counter of processed elements.
   * <p>
   * This value is later used as the denominator to calculate the migration ratio.
   *
   * @param processedElements the amount of elements processed
   */
  void addProcessedElements(int processedElements);

  /**
   * Returns the type of the migrated project.
   */
  String getProjectType();

  /**
   * Returns the name of the migrated project.
   */
  String getProjectName();

  /**
   * Returns a list of connectors migrated.
   */
  List<String> getConnectorNames();

  /**
   * Retrieves the migrated connectors from the POM.
   *
   * @param pomModel POM from where the migrated connectors are retrieved.
   */
  void addConnectors(PomModel pomModel);

  /**
   * Number of components successfully migrated.
   */
  Integer getComponentSuccessCount();

  /**
   * Number of components that where not migrated successfully.
   */
  Integer getComponentFailureCount();

  /**
   * Number of total components before migration.
   */
  Integer getComponentCount();

  /**
   * Migration status summary by component.
   */
  Map<String, ComponentMigrationStatus> getComponents();

  String getComponentKey(Element element);

  /**
   * Increments the component success migration count.
   *
   * @param element component migrated with no errors.
   */
  void addComponentSuccess(Element element);

  /**
   * Increments the component failure migration count.
   *
   * @param element component migrated with errors.
   */
  void addComponentFailure(Element element);

  /**
   * Number of DataWeave scripts migrated successfully.
   */
  Integer getDwTransformsSuccessCount();

  /**
   * Number of DataWeave scripts migrated unsuccessfully.
   */
  Integer getDwTransformsFailureCount();

  /**
   * Number of DataWeave scripts before migration.
   */
  Integer getDwTransformsCount();

  /**
   * Number of DataWeave script lines migrated successfully.
   */
  Integer getDwTransformsSuccessLineCount();

  /**
   * Number of DataWeave script lines migrated unsuccessfully.
   */
  Integer getDwTransformsFailureLineCount();

  /**
   * Number of DataWeave script lines before migration.
   */
  Integer getDwTransformsLineCount();

  /**
   * Reports a successful DataWeave script migration.
   *
   * @param script DataWeave script migrated.
   */
  void dwTransformsSuccess(String script);

  /**
   * Reports an unsuccessful DataWeave script migration.
   *
   * @param script DataWeave script unsuccessfully migrated.
   */
  void dwTransformsFailure(String script);

  /**
   * Number of MEL expressions migrated successfully.
   */
  Integer getMelExpressionsSuccessCount();

  /**
   * Number of MEL expressions migrated unsuccessfully.
   */
  Integer getMelExpressionsFailureCount();

  /**
   * Number of MEL expressions before migration
   */
  Integer getMelExpressionsCount();

  /**
   * Number of MEL expression lines migrated successfully.
   */
  Integer getMelExpressionsSuccessLineCount();

  /**
   * Number of MEL expression lines migrated unsuccessfully.
   */
  Integer getMelExpressionsFailureLineCount();


  /**
   * Number of MEL expression lines before migration.
   */
  Integer getMelExpressionsLineCount();

  /**
   * Reports a successful MEL expression migration.
   *
   * @param melExpression MEL expression migrated.
   */
  void melExpressionSuccess(String melExpression);

  /**
   * Reports an unsuccessful MEL expression migration.
   *
   * @param melExpression MEL expression unsuccessfully migrated.
   */
  void melExpressionFailure(String melExpression);

  /**
   * Update file reference on report
   *
   * @param oldFileName the previous name of the file
   * @param newFileName the new name of the file
   */
  void updateReportEntryFilePath(Path oldFileName, Path newFileName);

  /**
   * Returns the report entries.
   */
  List<T> getReportEntries();

  /**
   * Returns the report entries for the given levels.
   *
   * @param levels to filter report entries returned.
   */
  List<T> getReportEntries(Level... levels);

}
