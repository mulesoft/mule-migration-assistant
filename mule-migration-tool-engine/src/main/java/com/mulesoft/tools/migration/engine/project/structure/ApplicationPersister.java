/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.project.structure;

import com.mulesoft.tools.migration.engine.project.ProjectTypeFactory;
import com.mulesoft.tools.migration.engine.project.structure.mule.MuleProject;
import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourApplication;
import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourDomain;
import com.mulesoft.tools.migration.engine.project.structure.mule.three.MuleThreeApplication;
import com.mulesoft.tools.migration.engine.project.structure.mule.three.MuleThreeDomain;
import com.mulesoft.tools.migration.engine.project.structure.util.CopyFileVisitor;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.artifact.MuleArtifactJsonModel;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static com.mulesoft.tools.migration.engine.project.ProjectMatcher.getProjectDestination;
import static java.nio.file.Files.exists;

/**
 * Will save all the changes applied on the application after each task execution
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ApplicationPersister {

  private static ProjectTypeFactory projectFactory = new ProjectTypeFactory();

  private ApplicationModel appModel;
  private Path outputAppPath;
  private BasicProject projectOutput;
  private ProjectType projectType;

  public ApplicationPersister(ApplicationModel appModel, Path outputAppPath) throws Exception {
    this.setAppModel(appModel);
    this.setOutputAppPath(outputAppPath);
    this.setProjectType(projectFactory.getProjectType(appModel.getProjectBasePath()));
  }

  private void setProjectType(ProjectType projectType) {
    this.projectType = projectType;
  }

  private void setAppModel(ApplicationModel appModel) {
    this.appModel = appModel;
  }

  private void setOutputAppPath(Path outputAppPath) {
    this.outputAppPath = outputAppPath;
  }

  public void persist() throws Exception {
    projectOutput = getProjectDestination(outputAppPath, projectType);
    if (baseFolderIsEmpty(outputAppPath)) {
      copyBaseProjectStructure();
    }
    if (projectOutput instanceof MuleProject) {
      createSourcesFolders();
      persistConfigFiles();
      persistMuleArtifactJson();
      persistPom();
    }
  }

  private void persistConfigFiles() throws Exception {
    for (Map.Entry<Path, Document> entry : appModel.getApplicationDocuments().entrySet()) {
      Path originalFilePath = entry.getKey();
      String targetFilePath = getTargetFilePath(originalFilePath);

      Document document = entry.getValue();

      XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
      xmlOutputter.output(document, new FileOutputStream(targetFilePath));
    }
  }

  private String getTargetFilePath(Path originalFilePath) {
    if (originalFilePath.toString().contains(MuleThreeApplication.srcMainConfigurationPath)
        || originalFilePath.toString().contains(MuleFourApplication.srcMainConfigurationPath)) {
      return outputAppPath.resolve(((MuleProject) projectOutput).srcMainConfiguration())
          .resolve(originalFilePath.getFileName()).toString();
    } else if (originalFilePath.toString().contains(MuleThreeDomain.srcMainConfigurationPath)
        || originalFilePath.toString().contains(MuleFourDomain.srcMainConfigurationPath)) {
      return outputAppPath.resolve(((MuleProject) projectOutput).srcMainConfiguration())
          .resolve(originalFilePath.getFileName()).toString();
    } else {
      return outputAppPath.resolve(((MuleProject) projectOutput).srcTestConfiguration())
          .resolve(originalFilePath.getFileName()).toString();
    }
  }

  private void persistMuleArtifactJson() throws IOException {
    Optional<MuleArtifactJsonModel> muleArtifactJsonModel = appModel.getMuleArtifactJsonModel();
    if (muleArtifactJsonModel.isPresent() && projectOutput instanceof MuleFourApplication) {
      String jsonContent = appModel.getMuleArtifactJsonModel().get().toString();
      File outputFile = ((MuleFourApplication) projectOutput).muleArtifactJson().toFile();
      try (FileWriter fileWriter = new FileWriter(outputFile)) {
        fileWriter.write(jsonContent);
      }
    }
  }

  private void copyBaseProjectStructure() throws IOException {
    CopyFileVisitor vistor = new CopyFileVisitor(appModel.getProjectBasePath().toFile(), outputAppPath.toFile());
    Files.walkFileTree(appModel.getProjectBasePath(), vistor);
  }

  private void createSourcesFolders() {
    File app;
    MuleProject project = (MuleProject) projectOutput;
    if (!exists(project.srcMainConfiguration())) {
      app = project.srcMainConfiguration().toFile();
      app.mkdirs();
    }
    if (!(project instanceof MuleFourDomain)) {
      if (!exists(project.srcTestConfiguration())) {
        app = project.srcTestConfiguration().toFile();
        app.mkdirs();
      }
    }
  }

  private boolean baseFolderIsEmpty(Path project) {
    File app = project.toFile();
    return app.list().length <= 0;
  }

  private void persistPom() throws Exception {
    MavenXpp3Writer mavenWriter = new MavenXpp3Writer();
    Path pomLocation = ((MavenProject) projectOutput).pom();
    BufferedWriter writer = new BufferedWriter(new FileWriter(pomLocation.toFile()));
    if (appModel.getPomModel().isPresent()) {
      mavenWriter.write(writer, appModel.getPomModel().get().getMavenModelCopy());
    }
  }


}
