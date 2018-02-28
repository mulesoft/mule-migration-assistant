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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.mulesoft.tools.migration.project.model.ApplicationModel.ApplicationModelBuilder;
import com.mulesoft.tools.migration.project.structure.mule.three.MuleApplicationProject;

/**
 * @author Mulesoft Inc.
 */
public class ApplicationModelTest {

  private static final String ORIGINAL_PROJECT_NAME = "original-project";
  private static final String MIGRATED_PROJECT_NAME = "migrated-project";


  private static final String MUNIT_SECTIONS_SAMPLE_XML = "munit-sections-sample.xml";
  private static final Path MUNIT_EXAMPLES_PATH = Paths.get("munit/examples");
  private static final Path MUNIT_SECTIONS_SAMPLE_PATH = MUNIT_EXAMPLES_PATH.resolve(MUNIT_SECTIONS_SAMPLE_XML);

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private Path originalProjectPath;
  private Path migratedProjectPath;
  private MuleApplicationProject muleApplicationProject;

  @Before
  public void setUp() throws Exception {
    buildOriginalProject();
    migratedProjectPath = temporaryFolder.newFolder(MIGRATED_PROJECT_NAME).toPath();

    muleApplicationProject = new MuleApplicationProject(originalProjectPath);
  }

  private static final String XPATH_SELECTOR = "//munit:test/*[contains(local-name(),'true')]";

  @Test
  public void test1() throws Exception {
    ApplicationModel applicationModel = new ApplicationModelBuilder(muleApplicationProject).build();

    applicationModel.addNameSpace("munit-tools", "http://www.mulesoft.org/schema/mule/munit-tools");

    applicationModel.getNodes(XPATH_SELECTOR)
        .forEach(n -> changeNodeName("munit-tools", "assert-that")
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

}
