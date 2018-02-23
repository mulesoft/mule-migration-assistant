/*
 * Copyright (c) 2015 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.structure;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;

/**
 * Represent the most basic project
 * 
 * @author Mulesoft Inc.
 */
public class BasicProject {

  protected Path baseFolder;

  public BasicProject(Path baseFolder) {
    checkArgument(baseFolder != null, "The base folder path must not be null");
    checkArgument(baseFolder.toFile().exists() == true, "The base folder path must exists");
    checkArgument(baseFolder.toFile().isDirectory() == true, "The base folder path must be a folder");

    this.baseFolder = baseFolder;
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
