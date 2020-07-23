/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.soapkit;

import com.mulesoft.tools.migration.library.soapkit.steps.AbstractSoapkitMigrationStep;
import com.mulesoft.tools.migration.library.soapkit.tasks.SoapkitMigrationTask;
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
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.mulesoft.tools.migration.library.soapkit.helpers.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.library.soapkit.helpers.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

@RunWith(Parameterized.class)
public class SoapkitMigrationTest {

  private static Path BASE_PATH = Paths.get("mule/apps/soapkit");

  private final Path configPath;
  private final Path targetPath;

  @Parameterized.Parameters(name = "{0}")
  public static Object[] params() throws Exception {
    final URI resource = SoapkitMigrationTest.class.getClassLoader().getResource(BASE_PATH.toString()).toURI();
    return Files.walk(Paths.get(new File(resource).getAbsolutePath()))
        .filter(s -> s.toString().endsWith("-original.xml"))
        .map(p -> new File(p.toUri()).getName().replaceAll("-original.xml", ""))
        .sorted()
        .collect(toList())
        .toArray(new Object[] {});
  }

  public SoapkitMigrationTest(String filePrefix) {
    configPath = BASE_PATH.resolve(filePrefix + "-original.xml");
    targetPath = BASE_PATH.resolve(filePrefix + ".xml");
  }

  private List<MigrationStep> steps;

  @Before
  public void setUp() throws Exception {
    final ApplicationModel applicationModel = getApplicationModel();
    final SoapkitMigrationTask soapkitMigrationTask = new SoapkitMigrationTask();

    steps = soapkitMigrationTask.getSteps().stream()
        .filter(step -> step instanceof AbstractSoapkitMigrationStep)
        .collect(toList());

    steps.forEach(step -> ((AbstractSoapkitMigrationStep) step).setApplicationModel(applicationModel));
  }

  private static ApplicationModel getApplicationModel() throws Exception {
    final ApplicationModel mock = mock(ApplicationModel.class);
    doCallRealMethod().when(mock).removeNameSpace(any(Namespace.class), anyString(), any(Document.class));
    final URI basePathURI = SoapkitMigrationTest.class.getClassLoader().getResource(BASE_PATH.toString()).toURI();
    final Path projectBasePath = Paths.get(new File(basePathURI).getAbsolutePath());
    doReturn(projectBasePath).when(mock).getProjectBasePath();
    return mock;
  }

  @Test
  public void execute() throws Exception {
    Document doc =
        getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());

    steps.forEach(step -> getElementsFromDocument(doc, ((AbstractSoapkitMigrationStep) step).getAppliedTo().getExpression())
        .forEach(node -> step.execute(node, mock(MigrationReport.class))));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}

