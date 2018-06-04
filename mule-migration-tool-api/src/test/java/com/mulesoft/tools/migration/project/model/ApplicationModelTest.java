/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.model;

import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.addAttribute;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeAttribute;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.mulesoft.tools.migration.project.model.ApplicationModel.ApplicationModelBuilder;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mulesoft Inc.
 */
public class ApplicationModelTest {

  private static final String ORIGINAL_PROJECT_NAME = "original-project";
  private static final String MIGRATED_PROJECT_NAME = "migrated-project";
  private static final String MULE_VERSION = "4.1.1";
  private static final String MUNIT_SECTIONS_SAMPLE_XML = "munit-sections-sample.xml";
  private static final Path MUNIT_EXAMPLES_PATH = Paths.get("munit/examples");
  private static final Path MUNIT_SECTIONS_SAMPLE_PATH = MUNIT_EXAMPLES_PATH.resolve(MUNIT_SECTIONS_SAMPLE_XML);

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private Path originalProjectPath;
  private Path migratedProjectPath;

  @Before
  public void setUp() throws Exception {
    buildOriginalProject();
    migratedProjectPath = temporaryFolder.newFolder(MIGRATED_PROJECT_NAME).toPath();

  }

  private static final String XPATH_SELECTOR = "//munit:test/*[contains(local-name(),'true')]";

  @Test
  public void test1() throws Exception {
    ApplicationModel applicationModel = new ApplicationModelBuilder()
        .withProjectBasePath(originalProjectPath)
        .withConfigurationFiles(getFiles(originalProjectPath.resolve("src").resolve("main").resolve("app")))
        .withMuleVersion(MULE_VERSION)
        .withPom(originalProjectPath.resolve("pom.xml")).build();

    applicationModel.removeNameSpace("mock", "http://www.mulesoft.org/schema/mule/mock",
                                     "http://www.mulesoft.org/schema/mule/mock/current/mule-mock.xsd");
    applicationModel.addNameSpace("munit-tools", "http://www.mulesoft.org/schema/mule/munit-tools",
                                  "http://www.mulesoft.org/schema/mule/munit-tools/current/mule-munit-tools.xsd");

    applicationModel.getNodes(XPATH_SELECTOR).forEach(n -> changeNodeName("munit-tools", "assert-that")
        .andThen(changeAttribute("condition", of("expression"), empty()))
        .andThen(addAttribute("is", "#[equalTo(true)]"))
        .apply(n));
  }

  private void buildOriginalProject() throws IOException {
    originalProjectPath = temporaryFolder.newFolder(ORIGINAL_PROJECT_NAME).toPath();

    File app = originalProjectPath.resolve("src").resolve("main").resolve("app").toFile();
    app.mkdirs();

    URL sample = this.getClass().getClassLoader().getResource(MUNIT_SECTIONS_SAMPLE_PATH.toString());
    FileUtils.copyURLToFile(sample, new File(app, MUNIT_SECTIONS_SAMPLE_PATH.getFileName().toString()));
  }

  public static List<Path> getFiles(Path path, String... extensions) {
    String[] filter = extensions.length != 0 ? extensions : null;
    Collection<File> files = FileUtils.listFiles(path.toFile(), filter, true);
    return files.stream().map(f -> f.toPath()).collect(Collectors.toList());
  }

}
