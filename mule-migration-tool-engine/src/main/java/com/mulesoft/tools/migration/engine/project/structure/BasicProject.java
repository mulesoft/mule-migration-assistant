/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.engine.project.structure;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represent the most basic project
 * 
 * @author Mulesoft Inc.
 */
public class BasicProject {

  protected Path baseFolder;

  public BasicProject(Path baseFolder) {
    checkArgument(baseFolder != null, "The base folder path must not be null");

    if (!baseFolder.toFile().exists()) {
      baseFolder.toFile().mkdirs();
    }

    this.baseFolder = baseFolder.toAbsolutePath();
  }

  public Path getBaseFolder() {
    return baseFolder;
  }

  public static List<Path> getFiles(Path path, String... extensions) throws Exception {
    String[] filter = extensions.length != 0 ? extensions : null;
    Collection<File> files = FileUtils.listFiles(path.toFile(), filter, true);
    return files.stream().map(f -> f.toPath()).collect(Collectors.toList());
  }
}
