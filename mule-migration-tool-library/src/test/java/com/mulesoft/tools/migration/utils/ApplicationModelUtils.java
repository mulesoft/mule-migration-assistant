/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.utils;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.ApplicationModel.ApplicationModelBuilder;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ApplicationModelUtils {

  public static ApplicationModel generateAppModel(List<URL> applicationDocuments, Path projectPath) throws Exception {
    buildProject(applicationDocuments, projectPath);
    return new ApplicationModelBuilder().withProjectBasePath(projectPath).withPom(projectPath.resolve("pom.xml"))
        .withConfigurationFiles(getFiles(projectPath.resolve("src").resolve("main").resolve("app"))).build();
  }

  public static List<Path> getFiles(Path path, String... extensions) throws Exception {
    String[] filter = extensions.length != 0 ? extensions : null;
    Collection<File> files = FileUtils.listFiles(path.toFile(), filter, true);
    return files.stream().map(f -> f.toPath()).collect(Collectors.toList());
  }

  private static Path buildProject(List<URL> applicationDocuments, Path projectPath) throws IOException {
    File app = projectPath.resolve("src").resolve("main").resolve("app").toFile();
    app.mkdirs();

    for (URL documentPath : applicationDocuments) {
      FileUtils.copyURLToFile(documentPath, new File(app, documentPath.getFile()));
    }
    return projectPath;
  }
}
