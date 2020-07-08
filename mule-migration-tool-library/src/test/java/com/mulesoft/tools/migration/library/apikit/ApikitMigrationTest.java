/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.apikit;

import com.mulesoft.tools.migration.library.apikit.steps.AbstractApikitMigrationStep;
import com.mulesoft.tools.migration.library.apikit.tasks.ApikitMigrationTask;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

@RunWith(Parameterized.class)
public class ApikitMigrationTest {

  private static String BASE_PATH = "mule/apps/apikit";

  private final Path configPath;
  private final Path targetPath;

  @Parameterized.Parameters(name = "{0}")
  public static Object[] params() throws IOException {
    final URL resource = ApikitMigrationTest.class.getResource("/" + BASE_PATH);
    return Files.walk(Paths.get(resource.getPath()))
        .filter(s -> s.toString().endsWith("-original.xml"))
        .map(p -> new File(p.toUri()).getName().replaceAll("-original.xml", ""))
        .sorted()
        .collect(toList())
        .toArray(new Object[] {});
  }

  public ApikitMigrationTest(String filePrefix) {
    final Path path = Paths.get(BASE_PATH);
    configPath = path.resolve(filePrefix + "-original.xml");
    targetPath = path.resolve(filePrefix + ".xml");
  }

  private List<MigrationStep> steps;

  @Before
  public void setUp() {
    final ApplicationModel applicationModel = getApplicationModel();
    final ApikitMigrationTask apikitMigrationTask = new ApikitMigrationTask();

    steps = apikitMigrationTask.getSteps().stream()
        .filter(step -> step instanceof AbstractApikitMigrationStep)
        .collect(toList());

    steps.forEach(step -> ((AbstractApikitMigrationStep) step).setApplicationModel(applicationModel));
  }

  private static ApplicationModel getApplicationModel() {
    final ApplicationModel mock = mock(ApplicationModel.class);
    doCallRealMethod().when(mock).removeNameSpace(any(Namespace.class), anyString(), any(Document.class));
    return mock;
  }

  @Test
  public void execute() throws Exception {
    Document doc =
        getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());

    steps.forEach(step -> getElementsFromDocument(doc, ((AbstractApikitMigrationStep) step).getAppliedTo().getExpression())
        .forEach(node -> step.execute(node, mock(MigrationReport.class))));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}

