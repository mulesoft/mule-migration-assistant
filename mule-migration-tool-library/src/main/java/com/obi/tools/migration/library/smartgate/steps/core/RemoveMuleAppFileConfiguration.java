/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.steps.core;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import java.io.File;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

/**
 * Remove MULE_APP_FILE_NAME configuration and Replace it with mule.env properties
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RemoveMuleAppFileConfiguration extends AbstractApplicationModelMigrationStep {

  private static final String FILE = "file";
  private static final String MULE_APP_FILE_NAME = "mule-app.properties";
  private static final String MULE_APP_FILE_PATH =
      "src" + File.separator + "main" + File.separator + "resources" + File.separator + MULE_APP_FILE_NAME;
  public static final String XPATH_SELECTOR = getCoreXPathSelector("configuration-properties");


  @Override
  public String getDescription() {
    return "Migrate flow-ref components";
  }

  public RemoveMuleAppFileConfiguration() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {


    final Attribute attribute = element.getAttribute(FILE);
    if (attribute != null && attribute.getValue().equals(MULE_APP_FILE_NAME)) {
      attribute.setValue("${mule.env}.properties");
    }
    final ApplicationModel applicationModel = getApplicationModel();

    File muleAppProperties = new File(applicationModel.getProjectBasePath().toFile(), MULE_APP_FILE_PATH);
    muleAppProperties.delete();
  }
}
