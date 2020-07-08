/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.basicstructure;

import static com.mulesoft.tools.migration.step.util.ProjectStructureUtils.renameFile;
import static java.io.File.separator;
import static java.util.Arrays.stream;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.ProjectStructureContribution;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.io.filefilter.SuffixFileFilter;

/**
 * Rename Policy files
 *
 * @author Mulesoft Inc.
 */
public class PolicyFileRenameMigrationStep implements ProjectStructureContribution {

  private static final Path SOURCE_FILE_PATH = Paths.get("src" + separator + "main" + separator + "mule");

  private static final String XML_EXTENSION = ".xml";
  private static final String YAML_EXTENSION = ".yaml";
  private static final String YML_EXTENSION = ".yml";
  private static final String TEMPLATE_FILENAME = "template" + XML_EXTENSION;

  private ApplicationModel applicationModel;

  @Override
  public String getDescription() {
    return "Policy files rename step";
  }

  private Optional<File> getYamlFile(File projectBasePath) {
    return stream(projectBasePath.listFiles((FilenameFilter) new SuffixFileFilter(new String[] {YAML_EXTENSION, YML_EXTENSION})))
        .findFirst();
  }

  private void treatYaml(File projectBasePath, File yamlFile, String xmlFilename, MigrationReport migrationReport) {
    Optional<PomModel> pomModel = applicationModel.getPomModel();
    if (pomModel.isPresent()) {
      File newYamlFile = new File(projectBasePath, pomModel.get().getArtifactId() + YAML_EXTENSION);
      if (!newYamlFile.exists() && !new File(projectBasePath, pomModel.get().getArtifactId() + YML_EXTENSION).exists()) {
        yamlFile.renameTo(newYamlFile);
      }
    } else {
      migrationReport.report("basicStructure.noPomModel", null, null, xmlFilename);
    }
  }

  private void rename(File projectBasePath, File xmlFile, Path sourcesFilePath, MigrationReport migrationReport) {
    Optional<File> yamlFile = getYamlFile(projectBasePath);
    if (yamlFile.isPresent()) {
      treatYaml(projectBasePath, yamlFile.get(), xmlFile.getName(), migrationReport);
      renameFile(xmlFile.toPath(), sourcesFilePath.resolve(TEMPLATE_FILENAME), applicationModel,
                 migrationReport);
    } else {
      migrationReport.report("basicStructure.noYamlFound", null, null);
    }
  }

  @Override
  public void execute(Path path, MigrationReport migrationReport) throws RuntimeException {
    File projectBasePath = path.toFile();
    Path sourceFilesPath = path.resolve(SOURCE_FILE_PATH);
    if (sourceFilesPath.toFile().exists()) {
      Optional<File> policyTemplate =
          stream(sourceFilesPath.toFile().listFiles((FilenameFilter) new SuffixFileFilter(XML_EXTENSION)))
              .findFirst();
      if (policyTemplate.isPresent()) {
        rename(projectBasePath, policyTemplate.get(), sourceFilesPath, migrationReport);
      }
    }
  }

  @Override
  public ApplicationModel getApplicationModel() {
    return applicationModel;
  }

  public void setApplicationModel(ApplicationModel applicationModel) {
    this.applicationModel = applicationModel;
  }

}
