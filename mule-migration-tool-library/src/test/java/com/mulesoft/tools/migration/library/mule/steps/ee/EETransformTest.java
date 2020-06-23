/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.ee;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(Parameterized.class)
public class EETransformTest {

  private static final Path EE_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/ee");

  @Rule
  public ReportVerification report = new ReportVerification();

  @Parameterized.Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "ee-transform-01",
        "ee-transform-02",
        "ee-transform-03",
        "ee-transform-04",
        "ee-transform-05",
        "ee-transform-06",
        "ee-transform-07",
        "ee-transform-08"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public EETransformTest(String filePrefix) {
    configPath = EE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = EE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private EETransform eeTransform;

  @Before
  public void setUp() throws Exception {
    eeTransform = new EETransform();
    eeTransform.setApplicationModel(mock(ApplicationModel.class));
  }

  @Test
  public void execute() throws Exception {
    Document doc =
        getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    getElementsFromDocument(doc, eeTransform.getAppliedTo().getExpression())
        .forEach(node -> eeTransform.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
