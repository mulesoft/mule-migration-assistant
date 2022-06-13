/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.report;

import static com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationGlobalElements.MIGRATION_NAMESPACE;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.removeAllAttributes;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.removeAllAttributesRecursive;
import static java.util.Collections.emptyList;
import static java.util.Collections.list;

import com.mulesoft.tools.migration.exception.MigrationAbortException;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.report.html.model.ReportEntryModel;
import com.mulesoft.tools.migration.step.category.ComponentMigrationStatus;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.util.XmlDslUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Comment;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.yaml.snakeyaml.Yaml;

/**
 * Default implementation of a {@link MigrationReport}.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class DefaultMigrationReport implements MigrationReport<ReportEntryModel> {

  public static final Pattern WORD_MESSAGE_REPLACEMENT_EXPRESSION = Pattern.compile("\\{\\w*\\}");
  private transient Map<String, Map<String, Map<String, Object>>> possibleEntries;

  private transient XMLOutputter outp = new XMLOutputter();
  private final Set<ReportEntryModel> reportEntries = new LinkedHashSet<>();

  private String projectType;
  private String projectName;

  private double successfulMigrationRatio;
  private double errorMigrationRatio;
  private int processedElements;

  private final Map<String, ComponentMigrationStatus> components = new LinkedHashMap<>();
  private final Set<String> connectors = new LinkedHashSet<>();
  private int dwTransformsSuccess;
  private int dwTransformsFailure;
  private int dwTransformLinesSuccess;
  private int dwTransformLinesFailure;
  private int melExpressionsSuccess;
  private int melExpressionsFailure;
  private int melLinesSuccess;
  private int melLinesFailure;


  public DefaultMigrationReport() {
    possibleEntries = new HashMap<>();
    try {
      for (URL reportYamlUrl : list(DefaultMigrationReport.class.getClassLoader().getResources("report.yaml"))) {
        try (InputStream yamlStream = reportYamlUrl.openStream()) {
          possibleEntries.putAll(new Yaml().loadAs(yamlStream, Map.class));
        }
      }
    } catch (IOException e) {
      throw new MigrationAbortException("Couldn't load report entries definitions.", e);
    }
  }

  @Override
  public void initialize(ProjectType projectType, String projectName) {
    this.projectType = projectType.name();
    this.projectName = projectName;
  }

  @Override
  public void report(String entryKey, Element element, Element elementToComment, String... messageParams) {
    final String[] splitEntryKey = entryKey.split("\\.");

    final Map<String, Object> entryData = possibleEntries.get(splitEntryKey[0]).get(splitEntryKey[1]);

    final Level level = Level.valueOf((String) entryData.get("type"));
    final String message = (String) entryData.get("message");
    final Matcher matcher = WORD_MESSAGE_REPLACEMENT_EXPRESSION.matcher(message);
    final StringBuilder result = new StringBuilder();

    int i = 0;
    int currentIndex = 0;
    while (matcher.find(currentIndex)) {
      int start = matcher.start();
      result.append(message, currentIndex, start);
      if (messageParams.length > i) {
        result.append(messageParams[i]);
      }
      currentIndex = matcher.end();
      i++;
    }

    result.append(message.substring(currentIndex));

    final List<String> docLinks = entryData.get("docLinks") != null ? (List<String>) entryData.get("docLinks") : emptyList();
    report(entryKey, level, element, elementToComment, result.toString(), docLinks.toArray(new String[docLinks.size()]));
  }

  @Override
  public void report(Level level, Element element, Element elementToComment, String message, String... documentationLinks) {
    report(null, level, element, elementToComment, message, documentationLinks);
  }

  private void report(String entryKey, Level level, Element element, Element elementToComment, String message,
                      String... documentationLinks) {
    int i = 0;

    ReportEntryModel reportEntry;

    if (elementToComment != null) {
      if (elementToComment != element) {
        i = findContentIndex(element, elementToComment);
      }

      if (elementToComment.getDocument() != null || element.getDocument() == null) {
        reportEntry = new ReportEntryModel(entryKey, level, elementToComment, element, message, documentationLinks);
      } else {
        reportEntry =
            new ReportEntryModel(entryKey, level, elementToComment, element, message, element.getDocument(), documentationLinks);
      }

      if (reportEntries.add(reportEntry)) {
        elementToComment.addContent(i++, new Comment("Migration " + level.name() + ": " + sanitize(message)));

        if (documentationLinks.length > 0) {
          elementToComment.addContent(i++, new Comment("    For more information refer to:"));

          for (String link : documentationLinks) {
            elementToComment.addContent(i++, new Comment("        * " + sanitize(link)));
          }
        }

        if (element != elementToComment) {
          XmlDslUtils.removeNestedComments(element);
          removeAllAttributesRecursive(element, MIGRATION_NAMESPACE);
          elementToComment.addContent(i, new Comment(sanitize(outp.outputString(element))));
        }
      }
    }

  }

  private int findContentIndex(Element element, Element elementToComment) {
    int i = 0;

    while (i < elementToComment.getContent().size() && !element.toString().equals(elementToComment.getContent(i).toString())) {
      i++;
    }
    return i < elementToComment.getContent().size() ? i : 0;
  }

  private String sanitize(String message) {
    return message.replaceAll("--", " - - ");
  }

  @Override
  public void addProcessedElements(int processedElements) {
    this.processedElements += processedElements;
    this.successfulMigrationRatio = (1.0 * (this.processedElements - reportEntries.stream()
        .filter(re -> re.getElement() != null && !"compatibility".equals(re.getElement().getNamespacePrefix()))
        .map(re -> re.getElement()).distinct().count())) / this.processedElements;
    this.errorMigrationRatio = (1.0 * reportEntries.stream()
        .filter(re -> re.getElement() != null && ERROR.equals(re.getLevel()))
        .map(re -> re.getElement()).distinct().count()) / this.processedElements;
  }

  @Override
  public void updateReportEntryFilePath(Path oldFileName, Path newFileName) {
    reportEntries.stream().filter(e -> oldFileName.toString().equals(e.getFilePath()))
        .forEach(r -> r.setFilePath(newFileName.toString()));
  }

  public String getProjectType() {
    return projectType;
  }

  public String getProjectName() {
    return projectName;
  }

  @Override
  public List<ReportEntryModel> getReportEntries() {
    return new ArrayList<>(this.reportEntries);
  }

  @Override
  public List<ReportEntryModel> getReportEntries(Level... levels) {
    List<Level> levelList = Arrays.asList(levels);
    return reportEntries.stream().filter(e -> levelList.contains(e.getLevel())).collect(Collectors.toList());
  }

  public double getSuccessfulMigrationRatio() {
    return successfulMigrationRatio;
  }

  public double getErrorMigrationRatio() {
    return errorMigrationRatio;
  }

  @Override
  public List<String> getConnectorNames() {
    return new ArrayList<>(connectors);
  }

  @Override
  public void addConnectors(PomModel pomModel) {
    pomModel.getDependencies().stream()
        .filter(d -> d.getGroupId().contains("connector") || d.getArtifactId().contains("connector"))
        .forEach(d -> connectors.add(String.format("%s:%s:%s", d.getGroupId(), d.getArtifactId(), d.getVersion())));
  }

  @Override
  public Integer getComponentSuccessCount() {
    return components.values().stream().map(ComponentMigrationStatus::getSuccess).reduce(0, Integer::sum);
  }

  @Override
  public Integer getComponentFailureCount() {
    return components.values().stream().map(ComponentMigrationStatus::getFailure).reduce(0, Integer::sum);
  }

  @Override
  public Integer getComponentCount() {
    return getComponentSuccessCount() + getComponentFailureCount();
  }

  @Override
  public Map<String, ComponentMigrationStatus> getComponents() {
    return components;
  }

  @Override
  public String getComponentKey(Element element) {
    return DefaultMigrationReport.getComponentKeyStatic(element);
  }

  public static String getComponentKeyStatic(Element element) {
    String prefix = StringUtils.isBlank(element.getNamespace().getPrefix()) ? "" : element.getNamespace().getPrefix() + ":";
    return prefix + element.getName();
  }

  @Override
  public void addComponentSuccess(Element element) {
    String name = getComponentKey(element);
    components.putIfAbsent(name, new ComponentMigrationStatus());
    components.get(name).success();
  }

  @Override
  public void addComponentFailure(Element element) {
    String name = getComponentKey(element);
    components.putIfAbsent(name, new ComponentMigrationStatus());
    components.get(name).failure();
  }

  @Override
  public Integer getDwTransformsSuccessCount() {
    return dwTransformsSuccess;
  }

  @Override
  public Integer getDwTransformsFailureCount() {
    return dwTransformsFailure;
  }

  @Override
  public Integer getDwTransformsCount() {
    return dwTransformsSuccess + dwTransformsFailure;
  }


  @Override
  public Integer getDwTransformsSuccessLineCount() {
    return dwTransformLinesSuccess;
  }

  @Override
  public Integer getDwTransformsFailureLineCount() {
    return dwTransformLinesFailure;
  }

  @Override
  public Integer getDwTransformsLineCount() {
    return dwTransformLinesSuccess + dwTransformLinesFailure;
  }

  @Override
  public void dwTransformsSuccess(String script) {
    this.dwTransformsSuccess++;
    int lines = countLines(script);
    this.dwTransformLinesSuccess += lines;
  }

  @Override
  public void dwTransformsFailure(String script) {
    this.dwTransformsFailure++;
    int lines = countLines(script);
    this.dwTransformLinesFailure += lines;
  }

  @Override
  public Integer getMelExpressionsSuccessCount() {
    return melExpressionsSuccess;
  }

  @Override
  public Integer getMelExpressionsFailureCount() {
    return melExpressionsFailure;
  }

  @Override
  public Integer getMelExpressionsCount() {
    return melExpressionsFailure + melExpressionsSuccess;
  }

  @Override
  public Integer getMelExpressionsSuccessLineCount() {
    return melLinesSuccess;
  }

  @Override
  public Integer getMelExpressionsFailureLineCount() {
    return melLinesFailure;
  }

  @Override
  public Integer getMelExpressionsLineCount() {
    return melLinesFailure + melLinesSuccess;
  }

  @Override
  public void melExpressionSuccess(String melExpression) {
    this.melExpressionsSuccess++;
    int lines = countLines(melExpression);
    this.melLinesSuccess += lines;
  }

  @Override
  public void melExpressionFailure(String melExpression) {
    this.melExpressionsFailure++;
    int lines = countLines(melExpression);
    this.melLinesFailure += lines;
  }

  private int countLines(String melExpression) {
    return melExpression.split("\\r\\n|\\r|\\n").length;
  }

}
