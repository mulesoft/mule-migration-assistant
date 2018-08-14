/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.secureprops;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
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
import java.util.Collection;

@RunWith(Parameterized.class)
public class SecurePropertiesTest {

  private static final Path SECURE_PROPS_EXAMPLES_PATH = Paths.get("mule/apps/secureprops");

  @Parameters(name = "{0}, {1}")
  public static Collection<Object[]> data() {
    return asList(new Object[][] {
        {"secure-props-01", "4.1.3"},
        {"secure-props-02", "4.1.3"},
        {"secure-props-03", "4.1.3"},
        {"secure-props-04", "4.2.0"}
    });
  }

  private final Path configPath;
  private final Path targetMulePath;
  private final String muleVersion;

  public SecurePropertiesTest(String filePrefix, String muleVersion) {
    configPath = SECURE_PROPS_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetMulePath = SECURE_PROPS_EXAMPLES_PATH.resolve(filePrefix + ".xml");

    this.muleVersion = muleVersion;
  }

  private SecurePropertiesPlaceholder securePropertiesPlaceholder;

  @Before
  public void setUp() throws Exception {
    securePropertiesPlaceholder = new SecurePropertiesPlaceholder();
  }

  @Test
  public void execute() throws Exception {
    Path resolvedConfigPath = Paths.get(this.getClass().getClassLoader().getResource(configPath.toString()).toURI());
    ApplicationModel appModel =
        new ApplicationModelBuilder()
            .withProjectBasePath(Paths
                .get(this.getClass().getClassLoader().getResource(SECURE_PROPS_EXAMPLES_PATH.toString()).toURI()))
            .withConfigurationFiles(asList(resolvedConfigPath))
            .withMuleVersion(muleVersion)
            .build();

    Document doc = appModel.getApplicationDocuments().get(configPath.getFileName());

    securePropertiesPlaceholder.setApplicationModel(appModel);

    getElementsFromDocument(doc, securePropertiesPlaceholder.getAppliedTo().getExpression())
        .forEach(node -> securePropertiesPlaceholder.execute(node, mock(MigrationReport.class)));

    XMLOutputter muleOutputter = new XMLOutputter(Format.getPrettyFormat());
    String muleXmlString = muleOutputter.outputString(doc);

    assertThat(muleXmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetMulePath.toString()).toURI(),
                                            UTF_8))
                                                .ignoreComments().normalizeWhitespace());
  }

}
