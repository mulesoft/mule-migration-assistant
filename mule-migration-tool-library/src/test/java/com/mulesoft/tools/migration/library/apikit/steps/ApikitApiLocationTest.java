/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.apikit.steps;


import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

import static com.mulesoft.tools.migration.library.apikit.steps.ApikitApiLocation.MULE_3_API_FOLDER;
import static com.mulesoft.tools.migration.library.apikit.steps.ApikitApiLocation.MULE_4_API_FOLDER;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(Parameterized.class)
public class ApikitApiLocationTest {

  private static String BASE_PATH = "mule/apps/apikit/steps/apilocation/";

  private final String filePrefix;
  private Path projectPath;
  private Path originalProjectPath;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Parameterized.Parameters(name = "{0}")
  public static Object[] params() throws URISyntaxException {
    final URL resource = ApikitApiLocationTest.class.getResource("/" + BASE_PATH);
    final File[] files = new File(resource.toURI()).listFiles();

    return Arrays.stream(files)
        .filter(File::isDirectory)
        .map(File::getName)
        .collect(toList())
        .toArray(new Object[] {});
  }

  public ApikitApiLocationTest(String filePrefix) {
    this.filePrefix = filePrefix;
  }

  @Before
  public void setUp() throws IOException {
    // build project structure
    projectPath = temporaryFolder.newFolder(filePrefix).toPath();
    final File apiFolder = projectPath.resolve(MULE_3_API_FOLDER).toFile();
    apiFolder.mkdirs();

    // copy content to temp dir
    final File originalFolder = new File(ApikitApiLocation.class.getResource('/' + BASE_PATH + filePrefix).getFile());
    originalProjectPath = originalFolder.toPath();
    FileUtils.copyDirectory(originalFolder, projectPath.toFile());
  }


  @Test
  public void execute() throws Exception {
    new ApikitApiLocation().execute(projectPath, mock(MigrationReport.class));

    assertThat(projectPath.resolve(MULE_3_API_FOLDER).toFile().exists(), Matchers.equalTo(false));
    verifyDirsAreEqual(originalProjectPath.resolve(MULE_3_API_FOLDER), projectPath.resolve(MULE_4_API_FOLDER));

  }

  private static void verifyDirsAreEqual(Path one, Path other) throws IOException {
    Files.walkFileTree(one, new SimpleFileVisitor<Path>() {

      @Override
      public FileVisitResult visitFile(Path file,
                                       BasicFileAttributes attrs)
          throws IOException {
        FileVisitResult result = super.visitFile(file, attrs);

        // get the relative file name from path "one"
        Path relativize = one.relativize(file);
        // construct the path for the counterpart file in "other"
        Path fileInOther = other.resolve(relativize);

        byte[] otherBytes = Files.readAllBytes(fileInOther);
        byte[] thisBytes = Files.readAllBytes(file);
        assertThat(thisBytes, Matchers.equalTo(otherBytes));
        return result;
      }
    });
  }
}

