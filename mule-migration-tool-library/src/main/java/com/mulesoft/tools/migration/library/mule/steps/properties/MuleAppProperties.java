/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.properties;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.NamespaceContribution;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.xpath.XPathFactory;
import scala.App;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Migrate mule-app.properties references on Config files.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MuleAppProperties implements NamespaceContribution {

  private static final String MULE_APP_FILE_NAME = "mule-app.properties";
  private static final String MULE_PROPS_FILE =
      "src" + File.separator + "main" + File.separator + "resources" + File.separator + MULE_APP_FILE_NAME;

  @Override
  public String getDescription() {
    return "Migrate mule-app.properties references on Config files.";
  }

  @Override
  public void execute(ApplicationModel appModel, MigrationReport report) throws RuntimeException {
    try {
      List<String> properties = resolveProperties(appModel.getProjectBasePath(), MULE_PROPS_FILE);
      if (!properties.isEmpty()) {
        properties.forEach(p -> {
          appModel.getDocumentsContainString("${" + p + "}")
              .forEach(n -> addConfigFileReference(n, appModel));
          appModel.getDocumentsContainString("p(\\\"" + p + "\\\")")
              .forEach(n -> addConfigFileReference(n, appModel));
          appModel.getDocumentsContainString("p('" + p + "')")
              .forEach(n -> addConfigFileReference(n, appModel));
        });
      }
    } catch (IOException e) {
      throw new MigrationStepException("Could not update mule-app.properties references on Files.", e);

    }
  }

  private void addConfigFileReference(Document document, ApplicationModel appModel) {
    Element configProperties = new Element("configuration-properties", CORE_NAMESPACE);
    configProperties.setAttribute("file", MULE_APP_FILE_NAME);

    if (appModel.getElementsFromDocument(XPathFactory.instance().compile("//*[@file = '" + MULE_APP_FILE_NAME + "']"), document)
        .isEmpty()) {
      addTopLevelElement(configProperties, document);
    }
  }

  private List<String> resolveProperties(Path filePath, String propsFileName) throws IOException {
    File muleAppProperties = new File(filePath.toFile(), propsFileName);
    List<String> appProperties = new ArrayList<>();
    if (muleAppProperties != null && muleAppProperties.exists()) {
      try (FileInputStream inputStream = new FileInputStream(muleAppProperties)) {
        Properties properties = new Properties();
        properties.load(inputStream);
        properties.forEach((k, v) -> appProperties.add((String) k));
      }
    }
    return appProperties;
  }
}
