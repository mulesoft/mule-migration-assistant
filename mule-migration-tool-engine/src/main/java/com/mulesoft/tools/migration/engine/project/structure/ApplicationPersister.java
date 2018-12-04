/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.project.structure;

import static com.mulesoft.tools.migration.engine.project.ProjectMatcher.getProjectDestination;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_THREE_APPLICATION;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_THREE_MAVEN_APPLICATION;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.exists;
import static org.apache.commons.io.FileUtils.moveFileToDirectory;
import static org.jdom2.output.Format.getPrettyFormat;

import com.mulesoft.tools.migration.engine.exception.MigrationJobException;
import com.mulesoft.tools.migration.engine.project.ProjectTypeFactory;
import com.mulesoft.tools.migration.engine.project.structure.mule.MuleProject;
import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourApplication;
import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourDomain;
import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourPolicy;
import com.mulesoft.tools.migration.engine.project.structure.mule.three.MuleThreeApplication;
import com.mulesoft.tools.migration.engine.project.structure.mule.three.MuleThreeDomain;
import com.mulesoft.tools.migration.engine.project.structure.util.CopyFileVisitor;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.artifact.MuleArtifactJsonModel;

import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.jdom2.Document;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

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
      persistMuleAppProperties();
    }
  }

  private void persistMuleAppProperties() throws Exception {
    projectType = projectFactory.getProjectType(appModel.getProjectBasePath());
    if (projectType.equals(MULE_THREE_APPLICATION) || projectType.equals(MULE_THREE_MAVEN_APPLICATION)) {
      MuleThreeApplication project = new MuleThreeApplication(appModel.getProjectBasePath());
      Path source = project.appProperties();
      if (source.toFile().exists()) {
        Path resources = ((MuleProject) projectOutput).srcMainResources();
        resources.toFile().mkdirs();
        Files.copy(source, resources.resolve("mule-app.properties"));
      }
    }
  }

  private void persistConfigFiles() throws Exception {
    for (Map.Entry<Path, Document> entry : appModel.getApplicationDocuments().entrySet()) {
      Path originalFilePath = entry.getKey();
      Path targetFilePath = getTargetFilePath(originalFilePath);

      Document document = entry.getValue();

      XMLOutputter xmlOutputter = new XMLOutputter(getPrettyFormat().setIndent("    "));
      ByteArrayOutputStream preFromattedOutput = new ByteArrayOutputStream();
      xmlOutputter.output(document, preFromattedOutput);

      SAXBuilder saxBuilder = new SAXBuilder();
      Document finalDocument = saxBuilder.build(new ByteArrayInputStream(preFromattedOutput.toByteArray()));

      // Add empty lines between top level elements, as requested by our beloved PM <3
      new LinkedList<>(finalDocument.getRootElement().getChildren()).descendingIterator().forEachRemaining(c -> {
        finalDocument.getRootElement().addContent(finalDocument.getRootElement().indexOf(c) + 1,
                                                  new Text(lineSeparator()));

        if ("flow".equals(c.getName())) {
          new LinkedList<>(c.getChildren()).descendingIterator().forEachRemaining(fc -> {
            c.addContent(c.indexOf(fc) + 1, new Text(lineSeparator()));
          });
        }
      });
      finalDocument.getRootElement().addContent(0, new Text(lineSeparator()));

      File targetFile = targetFilePath.toFile();
      targetFile.getParentFile().mkdirs();
      new XMLOutputter().output(finalDocument, new FileOutputStream(targetFile));
    }
  }

  private Path getTargetFilePath(Path originalFilePath) throws MigrationJobException {
    if (originalFilePath.toString().startsWith(MuleThreeApplication.srcMainConfigurationPath)) {
      return outputAppPath.resolve(((MuleProject) projectOutput).srcMainConfiguration())
          .resolve(originalFilePath.toString().substring(MuleThreeApplication.srcMainConfigurationPath.length() + 1));
    } else if (originalFilePath.toString().startsWith(MuleThreeApplication.srcTestsConfigurationPath)) {
      return outputAppPath.resolve(((MuleProject) projectOutput).srcTestConfiguration())
          .resolve(originalFilePath.toString().substring(MuleThreeApplication.srcTestsConfigurationPath.length() + 1));
    } else if (originalFilePath.toString().startsWith(MuleFourApplication.srcMainConfigurationPath)) {
      return outputAppPath.resolve(((MuleProject) projectOutput).srcMainConfiguration())
          .resolve(originalFilePath.toString().substring(MuleFourApplication.srcMainConfigurationPath.length() + 1));
    } else if (originalFilePath.toString().startsWith(MuleThreeDomain.srcMainConfigurationPath)) {
      return outputAppPath.resolve(((MuleProject) projectOutput).srcMainConfiguration())
          .resolve(originalFilePath.toString().substring(MuleThreeDomain.srcMainConfigurationPath.length() + 1));
    } else if (originalFilePath.toString().startsWith(MuleFourDomain.srcMainConfigurationPath)) {
      return outputAppPath.resolve(((MuleProject) projectOutput).srcMainConfiguration())
          .resolve(originalFilePath.toString().substring(MuleFourDomain.srcMainConfigurationPath.length() + 1));
    } else if (projectOutput instanceof MuleFourPolicy) {
      try {
        moveFileToDirectory(outputAppPath.resolve(originalFilePath).toFile(),
                            ((MuleProject) projectOutput).srcMainConfiguration().toFile(), true);
      } catch (IOException e) {
        throw new MigrationJobException("Cannot create policy structure", e);
      }

      return outputAppPath.resolve(((MuleProject) projectOutput).srcMainConfiguration())
          .resolve(originalFilePath.toString());
    } else if (originalFilePath.toString().startsWith(MuleFourApplication.srcMainResourcesPath)) {
      return outputAppPath.resolve(originalFilePath);
    } else {
      return outputAppPath.resolve(((MuleProject) projectOutput).srcTestConfiguration())
          .resolve(originalFilePath.getFileName().toString());
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
    if (!(project instanceof MuleFourDomain || project instanceof MuleFourPolicy)) {
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
