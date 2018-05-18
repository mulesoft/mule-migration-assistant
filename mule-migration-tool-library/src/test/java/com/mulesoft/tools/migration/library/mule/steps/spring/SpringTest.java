/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.spring;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.ApplicationModel.ApplicationModelBuilder;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(Parameterized.class)
public class SpringTest {

  private static final Path SPRING_EXAMPLES_PATH = Paths.get("mule/apps/spring");


  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "spring-01",
        "spring-02",
        "spring-03",
        "spring-04",
        "spring-05",
        "spring-06",
        "spring-07",
        "spring-08",
        "spring-09",
        "spring-10"
    };
  }

  private final Path configPath;
  private final Path targetMulePath;
  private final Path targetSpringPath;

  public SpringTest(String filePrefix) {
    configPath = SPRING_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetMulePath = SPRING_EXAMPLES_PATH.resolve(filePrefix + "-mule.xml");
    targetSpringPath = SPRING_EXAMPLES_PATH.resolve(filePrefix + "-original-beans.xml");
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
            .build();

    Document doc = appModel.getApplicationDocuments().get(configPath.getFileName());

    springPropertiesPlaceholder.setApplicationModel(appModel);
    springConfigContainingMuleConfig.setApplicationModel(appModel);
    springConfigInMuleConfig.setApplicationModel(appModel);
    springBeans.setApplicationModel(appModel);
    springContext.setApplicationModel(appModel);
    springContributions.setApplicationModel(appModel);

    getElementsFromDocument(doc, springPropertiesPlaceholder.getAppliedTo().getExpression())
        .forEach(node -> springPropertiesPlaceholder.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, springConfigContainingMuleConfig.getAppliedTo().getExpression(), "spring")
        .forEach(node -> springConfigContainingMuleConfig.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, springConfigInMuleConfig.getAppliedTo().getExpression())
        .forEach(node -> springConfigInMuleConfig.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, springBeans.getAppliedTo().getExpression())
        .forEach(node -> springBeans.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, springContext.getAppliedTo().getExpression())
        .forEach(node -> springContext.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, springContributions.getAppliedTo().getExpression())
        .forEach(node -> springContributions.execute(node, mock(MigrationReport.class)));

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
