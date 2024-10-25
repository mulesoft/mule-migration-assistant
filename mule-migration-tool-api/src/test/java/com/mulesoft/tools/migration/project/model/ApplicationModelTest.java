/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_THREE_APPLICATION;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.addAttribute;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeAttribute;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mulesoft.tools.migration.project.model.ApplicationModel.ApplicationModelBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
        .withSupportedNamespaces(newArrayList())
        .withProjectType(MULE_THREE_APPLICATION)
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

  @Test
  public void testAddNameSpace_schemaLocationAttribute_isNull() throws Exception {


    Document document = mock(Document.class);
    Element element = mock(Element.class);
    Namespace namespace = Namespace.getNamespace("mock", "http://www.mulesoft.org/schema/mule/mock");

    when(document.getRootElement()).thenReturn(element);

    ApplicationModel.addNameSpace(namespace,
                                  "http://www.mulesoft.org/schema/mule/mock/current/mule-mock.xsd", document);

  }

  @Test
  public void testremoveNameSpace_schemaLocationAttribute_isNull() throws Exception {

    ApplicationModel applicationModel = new ApplicationModelBuilder()
        .withProjectBasePath(originalProjectPath)
        .withConfigurationFiles(getFiles(originalProjectPath.resolve("src").resolve("main").resolve("app")))
        .withMuleVersion(MULE_VERSION)
        .withSupportedNamespaces(newArrayList())
        .withProjectType(MULE_THREE_APPLICATION)
        .withPom(originalProjectPath.resolve("pom.xml")).build();

    Document document = mock(Document.class);
    Element element = mock(Element.class);
    Namespace namespace = Namespace.getNamespace("mock", "http://www.mulesoft.org/schema/mule/mock");

    when(document.getRootElement()).thenReturn(element);

    applicationModel.removeNameSpace(namespace,
                                     "http://www.mulesoft.org/schema/mule/mock/current/mule-mock.xsd", document);

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
