/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.properties;

import com.google.common.collect.Iterables;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.tck.ReportVerification;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.mulesoft.tools.migration.utils.ApplicationModelUtils.generateAppModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

@RunWith(Parameterized.class)
public class MuleAppPropertiesTest {

  private static final Path CORE_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/properties");
  private static final String APP_NAME = "prop-app";

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  @Parameterized.Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "properties-01",
        "properties-02",
        "properties-03",
        "properties-04",
        "properties-05"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public MuleAppPropertiesTest(String filePrefix) {
    configPath = CORE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = CORE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private MuleAppProperties muleAppProperties;
  private Path appPath;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    muleAppProperties = new MuleAppProperties();
    buildProject();
    appModel = generateAppModel(appPath);
  }

  private void buildProject() throws IOException {
    appPath = temp.newFolder(APP_NAME).toPath();
    File app = appPath.resolve("src").resolve("main").resolve("app").toFile();
    app.mkdirs();

    URL sample = this.getClass().getClassLoader().getResource(configPath.toString());
    FileUtils.copyURLToFile(sample, new File(app, configPath.getFileName().toString()));

    File resources = new File(appPath.toFile(), "src/main/resources");
    resources.mkdirs();
    File muleAppProperties = new File(resources, "mule-app.properties");
    FileUtils.write(muleAppProperties, "lala=pepe \nsample=lolo", UTF_8);
  }

  @Test
  public void execute() throws Exception {
    muleAppProperties.execute(appModel, mock(MigrationReport.class));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(Iterables.get(appModel.getApplicationDocuments().values(), 0));

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
