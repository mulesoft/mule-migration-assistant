/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
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

@RunWith(Parameterized.class)
public class SpringSecurityTest {

  private static final Path SPRING_SECURITY_EXAMPLES_PATH = Paths.get("mule/apps/spring");

  @Rule
  public ReportVerification report = new ReportVerification();

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "spring-security-01",
        "spring-security-02",
        "spring-security-03",
        "spring-security-04"
    };
  }

  private final Path configPath;
  private final Path targetMulePath;
  private final Path targetSpringPath;

  public SpringSecurityTest(String filePrefix) {
    configPath = SPRING_SECURITY_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetMulePath = SPRING_SECURITY_EXAMPLES_PATH.resolve(filePrefix + "-mule.xml");
    targetSpringPath = SPRING_SECURITY_EXAMPLES_PATH.resolve(filePrefix + "-original-beans.xml");
  }

  private SpringConfigInMuleConfig springConfigInMuleConfig;
  private SecurityManager securityManager;
  private AuthorizationFilter authorizationFilter;

  @Before
  public void setUp() throws Exception {
    springConfigInMuleConfig = new SpringConfigInMuleConfig();
    securityManager = new SecurityManager();
    authorizationFilter = new AuthorizationFilter();
  }

  @Test
  public void execute() throws Exception {
    Path resolvedConfigPath = Paths.get(this.getClass().getClassLoader().getResource(configPath.toString()).toURI());
    ApplicationModel appModel =
        new ApplicationModelBuilder()
            .withProjectBasePath(Paths
                .get(this.getClass().getClassLoader().getResource(SPRING_SECURITY_EXAMPLES_PATH.toString()).toURI()))
            .withConfigurationFiles(asList(resolvedConfigPath))
            .withProjectType(MULE_FOUR_APPLICATION)
            .build();

    Document doc = appModel.getApplicationDocuments().get(configPath.getFileName());

    springConfigInMuleConfig.setApplicationModel(appModel);
    securityManager.setApplicationModel(appModel);

    getElementsFromDocument(doc, springConfigInMuleConfig.getAppliedTo().getExpression())
        .forEach(node -> springConfigInMuleConfig.execute(node, report.getReport()));
    getElementsFromDocument(doc, securityManager.getAppliedTo().getExpression())
        .forEach(node -> securityManager.execute(node, report.getReport()));
    getElementsFromDocument(doc, authorizationFilter.getAppliedTo().getExpression())
        .forEach(node -> authorizationFilter.execute(node, report.getReport()));

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
