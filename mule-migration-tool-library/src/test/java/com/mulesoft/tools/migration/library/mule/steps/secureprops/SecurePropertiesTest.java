/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.secureprops;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.security.properties.SecurePropertiesPlaceholder;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.ApplicationModel.ApplicationModelBuilder;
import com.mulesoft.tools.migration.tck.ReportVerification;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

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

@RunWith(Parameterized.class)
public class SecurePropertiesTest {

  private static final Path SECURE_PROPS_EXAMPLES_PATH = Paths.get("mule/apps/secureprops");

  @Rule
  public ReportVerification report = new ReportVerification();

  @Parameters(name = "{0}, {1}")
  public static Collection<Object[]> data() {
    return asList(new Object[][] {
        {"secure-props-01", "4.1.3"},
        {"secure-props-02", "4.1.3"},
        {"secure-props-03", "4.1.3"},
        {"secure-props-04", "4.2.0"},
        {"secure-props-05", "4.2.0"}
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
            .withProjectType(MULE_FOUR_APPLICATION)
            .build();

    Document doc = appModel.getApplicationDocuments().get(configPath.getFileName());

    securePropertiesPlaceholder.setApplicationModel(appModel);

    getElementsFromDocument(doc, securePropertiesPlaceholder.getAppliedTo().getExpression())
        .forEach(node -> securePropertiesPlaceholder.execute(node, report.getReport()));

    XMLOutputter muleOutputter = new XMLOutputter(Format.getPrettyFormat());
    String muleXmlString = muleOutputter.outputString(doc);

    assertThat(muleXmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetMulePath.toString()).toURI(),
                                            UTF_8))
                                                .ignoreComments().normalizeWhitespace());
  }

}
