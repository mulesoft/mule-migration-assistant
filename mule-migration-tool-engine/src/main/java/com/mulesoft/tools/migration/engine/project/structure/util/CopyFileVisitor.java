/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.engine.project.structure.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

/**
 * A visitor of files
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CopyFileVisitor implements FileVisitor<Path> {

  private final File fromFolder;
  private final File targetFolder;

  public CopyFileVisitor(File fromFolder, File targetFolder) {
    this.fromFolder = fromFolder;
    this.targetFolder = targetFolder;
  }

  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    if (doExclude(fromFolder.toPath().relativize(dir))) {
      return FileVisitResult.SKIP_SUBTREE;
    }
    Path targetPath = targetFolder.toPath().resolve(fromFolder.toPath().relativize(dir));
    if (!Files.exists(targetPath)) {
      Files.createDirectory(targetPath);
    }
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    if (doExclude(fromFolder.toPath().relativize(file))) {
      return FileVisitResult.SKIP_SUBTREE;
    }
    Files.copy(file, targetFolder.toPath().resolve(fromFolder.toPath().relativize(file)), StandardCopyOption.REPLACE_EXISTING);
    return FileVisitResult.CONTINUE;
  }

  private Boolean doExclude(Path path) {
    return Arrays.stream(Exclusions.values()).anyMatch(e -> (File.separator + path.toString()).equals(e.exclusion()));
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
