/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.utils;

import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.ApplicationModel.ApplicationModelBuilder;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

public class ApplicationModelUtils {

  public static ApplicationModel generateAppModel(Path projectPath) throws Exception {
    return new ApplicationModelBuilder()
        .withProjectBasePath(projectPath)
        .withPom(projectPath.resolve("pom.xml"))
        .withConfigurationFiles(getFiles(projectPath.resolve("src").resolve("main").resolve("app")))
        .withProjectType(MULE_FOUR_APPLICATION)
        .build();
  }

  public static List<Path> getFiles(Path path, String... extensions) throws Exception {
    String[] filter = extensions.length != 0 ? extensions : null;
    Collection<File> files = FileUtils.listFiles(path.toFile(), filter, true);
    return files.stream().map(f -> f.toPath()).collect(Collectors.toList());
  }
}
