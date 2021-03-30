/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.steps.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.NamespaceContribution;

/**
 * Migrate all properties placeholders with secure placeholders.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ReplaceStageAppPropertiesWithSecureProperties implements NamespaceContribution {

  private static final String SMARGATE_STAGE_PROD_FILE_NAME = "prod.properties";
  private static final String SMARTGATE_FILE_PATH =
      "src" + File.separator + "main" + File.separator + "resources" + File.separator + SMARGATE_STAGE_PROD_FILE_NAME;

  @Override
  public String getDescription() {
    return "Migrate all properties placeholders with secure placeholders.";
  }

  @Override
  public void execute(ApplicationModel appModel, MigrationReport report) throws RuntimeException {
    try {
      List<String> properties = resolveProperties(appModel.getProjectBasePath(), SMARTGATE_FILE_PATH);
      if (!properties.isEmpty()) {
        properties.forEach(p -> {
          appModel.getDocumentsContainString("${" + p + "}").forEach(n -> preplaceProperties(n, appModel, p, report));
        });
      }
    } catch (IOException e) {
      throw new MigrationStepException("Could not update placholders inside a mule application file.", e);

    }
  }

  private void preplaceProperties(Document document, ApplicationModel appModel, String property, MigrationReport report) {
    List<Element> children = document.getRootElement().getChildren();
    for (Element element : children) {
      List<Attribute> attributes = element.getAttributes();
      for (Attribute attribute : attributes) {
        if (attribute.getValue().equals("${" + property + "}")) {
          attribute.setValue("${secure::" + property + "}");
        }
      }
    }
  }


  private List<String> resolveProperties(Path filePath, String propsFileName) throws IOException {
    File muleAppProperties = new File(filePath.toFile(), propsFileName);
    List<String> appProperties = new ArrayList<>();
    if (muleAppProperties != null && muleAppProperties.exists()) {
      Properties properties = new Properties();
      properties.load(new FileInputStream(muleAppProperties));
      properties.forEach((k, v) -> appProperties.add((String) k));
    }
    return appProperties;
  }
}
