/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.structure;

import com.mulesoft.tools.migration.engine.structure.util.CopyFileVisitor;
import com.mulesoft.tools.migration.project.ProjectTypeFactory;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.structure.BasicProject;
import com.mulesoft.tools.migration.project.structure.MavenProject;
import com.mulesoft.tools.migration.project.structure.mule.MuleProject;
import com.mulesoft.tools.migration.project.structure.mule.four.MuleFourDomain;
import com.mulesoft.tools.migration.project.structure.mule.three.MuleThreeApplication;
import com.mulesoft.tools.migration.project.structure.mule.three.MuleThreeDomain;
import org.apache.commons.io.IOUtils;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static com.mulesoft.tools.migration.project.ProjectMatcher.getProjectDestination;
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

  public ApplicationPersister(ApplicationModel appModel, Path outputAppPath) {
    this.setAppModel(appModel);
    this.setOutputAppPath(outputAppPath);
  }

  private void setAppModel(ApplicationModel appModel) {
    this.appModel = appModel;
  }

  private void setOutputAppPath(Path outputAppPath) {
    this.outputAppPath = outputAppPath;
  }

  public void persist() throws Exception {
    projectOutput = getProjectDestination(outputAppPath, projectFactory.getProjectType(appModel.getProjectBasePath()));
    if (baseFolderIsEmpty(outputAppPath)) {
      copyBaseProjectStructure();
    }
    if (projectOutput instanceof MuleProject) {
      createSourcesFolders();
      persistConfigFiles();
      createMuleArtifactJsonFile();
      persistPom();
    }
  }

  private void persistConfigFiles() throws Exception {
    for (Map.Entry<Path, Document> entry : appModel.getApplicationDocuments().entrySet()) {
      Path originalFilePath = entry.getKey();
      Document document = entry.getValue();
      String targetFilePath;

      if (originalFilePath.toString().contains(MuleThreeApplication.srcMainConfigurationPath)) {
        targetFilePath = outputAppPath.resolve(((MuleProject) projectOutput).srcMainConfiguration())
            .resolve(originalFilePath.getFileName()).toString();
      } else if (originalFilePath.toString().contains(MuleThreeDomain.srcMainConfigurationPath)) {
        targetFilePath = outputAppPath.resolve(((MuleProject) projectOutput).srcMainConfiguration())
            .resolve(originalFilePath.getFileName()).toString();
      } else {
        targetFilePath = outputAppPath.resolve(((MuleProject) projectOutput).srcTestConfiguration())
            .resolve(originalFilePath.getFileName()).toString();
      }
      XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
      xmlOutputter.output(document, new FileOutputStream(targetFilePath));
    }
  }

  private void createMuleArtifactJsonFile() throws IOException {
    //TODO - Improve this, we need to define a project model and contribute this json file as a migration task
    String jsonContent = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("mule-artifact-sample.json"));
    File outputFile = new File(outputAppPath.resolve("mule-artifact.json").toString());
    FileWriter fileWriter = new FileWriter(outputFile);
    fileWriter.write(jsonContent);
    fileWriter.flush();
    fileWriter.close();
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
