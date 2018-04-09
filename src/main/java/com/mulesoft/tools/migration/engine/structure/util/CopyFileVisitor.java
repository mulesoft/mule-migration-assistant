/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.structure.util;

import com.mulesoft.tools.migration.project.structure.mule.four.MuleFourApplication;
import com.mulesoft.tools.migration.project.structure.mule.three.MuleThreeApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * A visitor of files
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CopyFileVisitor implements FileVisitor<Path> {

  private final File fromFolder;
  private final File targetFolder;

  private final List<Path> exclusions;

  public CopyFileVisitor(File fromFolder, File targetFolder) {
    this.fromFolder = fromFolder;
    this.targetFolder = targetFolder;
    this.exclusions = new ArrayList<>();
    this.exclusions.add(Paths.get(MuleThreeApplication.srcMainConfigurationPath));
    this.exclusions.add(Paths.get(MuleThreeApplication.srcTestsConfigurationPath));
    this.exclusions.add(Paths.get(MuleFourApplication.srcMainConfigurationPath));
    this.exclusions.add(Paths.get(MuleFourApplication.srcTestConfigurationPath));

  }

  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    for (Path excludePath : exclusions) {
      if (dir.toString().contains(excludePath.toString())) {
        return FileVisitResult.SKIP_SUBTREE;
      }
    }
    Path targetPath = targetFolder.toPath().resolve(fromFolder.toPath().relativize(dir));
    if (!Files.exists(targetPath)) {
      Files.createDirectory(targetPath);
    }
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    Files.copy(file, targetFolder.toPath().resolve(fromFolder.toPath().relativize(file)), StandardCopyOption.REPLACE_EXISTING);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(Path file, IOException exc) {
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
    return FileVisitResult.CONTINUE;
  }
}
