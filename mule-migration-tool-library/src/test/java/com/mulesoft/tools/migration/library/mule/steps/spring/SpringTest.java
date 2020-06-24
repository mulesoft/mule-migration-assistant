/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.spring;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.ApplicationModel.ApplicationModelBuilder;
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
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

@RunWith(Parameterized.class)
public class SpringTest {

  private static final Path SPRING_EXAMPLES_PATH = Paths.get("mule/apps/spring");

  @Rule
  public ReportVerification report = new ReportVerification();

  @Parameters(name = "{0}, {1}")
  public static Collection<Object[]> data() {
    return asList(new Object[][] {
        {"spring-01", "4.1.3"},
        {"spring-02", "4.1.3"},
        {"spring-03", "4.1.3"},
        {"spring-04", "4.1.3"},
        {"spring-05", "4.1.3"},
        {"spring-06", "4.1.3"},
        {"spring-07", "4.1.3"},
        {"spring-08", "4.1.3"},
        {"spring-09", "4.1.3"},
        {"spring-10", "4.1.3"},
        {"spring-11", "4.2.0"},
        {"spring-12", "4.2.0"}
    });
  }

  private final Path configPath;
  private final Path targetMulePath;
  private final Path targetSpringPath;
  private final String muleVersion;

  public SpringTest(String filePrefix, String muleVersion) {
    configPath = SPRING_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetMulePath = SPRING_EXAMPLES_PATH.resolve(filePrefix + "-mule.xml");
    targetSpringPath = SPRING_EXAMPLES_PATH.resolve(filePrefix + "-original-beans.xml");

    this.muleVersion = muleVersion;
  }

  private SpringPropertiesPlaceholder springPropertiesPlaceholder;
  private SpringConfigContainingMuleConfig springConfigContainingMuleConfig;
  private SpringConfigInMuleConfig springConfigInMuleConfig;
  private SpringBeans springBeans;
  private SpringContext springContext;
  private SpringContributions springContributions;

  @Before
  public void setUp() throws Exception {
    springPropertiesPlaceholder = new SpringPropertiesPlaceholder();
    springConfigContainingMuleConfig = new SpringConfigContainingMuleConfig();
    springConfigInMuleConfig = new SpringConfigInMuleConfig();
    springBeans = new SpringBeans();
    springContext = new SpringContext();
    springContributions = new SpringContributions();
  }

  @Test
  public void execute() throws Exception {
    Path resolvedConfigPath = Paths.get(this.getClass().getClassLoader().getResource(configPath.toString()).toURI());
    ApplicationModel appModel =
        new ApplicationModelBuilder()
            .withProjectBasePath(Paths.get(this.getClass().getClassLoader().getResource(SPRING_EXAMPLES_PATH.toString()).toURI()))
            .withConfigurationFiles(asList(resolvedConfigPath))
            .withMuleVersion(muleVersion)
            .withProjectType(MULE_FOUR_APPLICATION)
            .build();

    Document doc = appModel.getApplicationDocuments().get(configPath.getFileName());

    springPropertiesPlaceholder.setApplicationModel(appModel);
    springConfigContainingMuleConfig.setApplicationModel(appModel);
    springConfigInMuleConfig.setApplicationModel(appModel);
    springBeans.setApplicationModel(appModel);
    springContext.setApplicationModel(appModel);
    springContributions.setApplicationModel(appModel);

    getElementsFromDocument(doc, springPropertiesPlaceholder.getAppliedTo().getExpression())
        .forEach(node -> springPropertiesPlaceholder.execute(node, report.getReport()));
    getElementsFromDocument(doc, springConfigContainingMuleConfig.getAppliedTo().getExpression(), "spring")
        .forEach(node -> springConfigContainingMuleConfig.execute(node, report.getReport()));
    getElementsFromDocument(doc, springConfigInMuleConfig.getAppliedTo().getExpression())
        .forEach(node -> springConfigInMuleConfig.execute(node, report.getReport()));
    getElementsFromDocument(doc, springBeans.getAppliedTo().getExpression())
        .forEach(node -> springBeans.execute(node, report.getReport()));
    getElementsFromDocument(doc, springContext.getAppliedTo().getExpression())
        .forEach(node -> springContext.execute(node, report.getReport()));
    getElementsFromDocument(doc, springContributions.getAppliedTo().getExpression())
        .forEach(node -> springContributions.execute(node, report.getReport()));

    XMLOutputter muleOutputter = new XMLOutputter(Format.getPrettyFormat());
    String muleXmlString = muleOutputter.outputString(doc);

    assertThat(muleXmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetMulePath.toString()).toURI(),
                                            UTF_8))
                                                .ignoreComments().normalizeWhitespace());

    Document springDoc = appModel.getApplicationDocuments()
        .get(Paths.get("src/main/resources/spring", targetSpringPath.getFileName().toString()));

    if (this.getClass().getClassLoader().getResource(targetSpringPath.toString()) != null) {

      XMLOutputter springOutputter = new XMLOutputter(Format.getPrettyFormat());
      String springXmlString = springOutputter.outputString(springDoc);

      assertThat(springXmlString,
                 isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetSpringPath.toString()).toURI(),
                                              UTF_8))
                                                  .ignoreComments().normalizeWhitespace());
    } else {
      assertThat(springDoc, nullValue());
    }
  }

}
