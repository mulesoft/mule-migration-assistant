/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.spring;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map.Entry;

/**
 * Migrates the spring beans form the mule config to its own file.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SpringBeans extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "/mule:mule//http:request";

  @Override
  public String getDescription() {
    return "Migrates the spring beans form the mule config to its own file.";
  }

  public SpringBeans() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Path beansPath = null;
    Document springDocument = null;

    for (Entry<Path, Document> entry : getApplicationModel().getApplicationDocuments().entrySet()) {
      if (object.getDocument().equals(entry.getValue())) {
        beansPath = entry.getKey().getParent().resolve(entry.getKey().getFileName().toString().replace("\\.xml", "-beans.xml"));

        try {
          SAXBuilder saxBuilder = new SAXBuilder();
          springDocument =
              saxBuilder.build(SpringBeans.class.getClassLoader().getResourceAsStream("/spring/empty-beans.xml"));
        } catch (JDOMException | IOException e) {
          throw new MigrationStepException(e.getMessage(), e);
        }
        break;
      }
    }

    if (beansPath == null) {
      throw new MigrationStepException("The document of the passed element was not present in the application model");
    }

    getApplicationModel().getApplicationDocuments().put(beansPath, springDocument);
  }

  // spring:property nested in mule objects (certain elements only) change to mule:property
  // spring:beans root, mule top level
  // mule or domain root, spring:bean top level
  // mule or domain root, spring:beans top level
  // spring:bean or spring:property nested in cxf
  // spring-security?
  // spring-placeholders?
}
