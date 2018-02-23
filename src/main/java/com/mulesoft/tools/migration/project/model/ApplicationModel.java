/*
 * Copyright (c) 2015 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.model;

import static com.mulesoft.tools.migration.project.structure.BasicProject.getFiles;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.mulesoft.tools.migration.project.structure.mule.three.MuleApplicationProject;

/**
 * Represent the application to be migrated
 * 
 * @author Mulesoft Inc.
 */
public class ApplicationModel {

  private Map<Path, Document> applicationDocuments;

  private ApplicationModel(Map<Path, Document> applicationDocuments) {
    this.applicationDocuments = applicationDocuments;
  }

  public Map<Path, Document> getApplicationDocuments() {
    return applicationDocuments;
  }

  public static class ApplicationModelBuilder {

    private MuleApplicationProject project;

    public ApplicationModelBuilder(MuleApplicationProject project) {
      this.project = project;
    }

    public ApplicationModel build() throws Exception {
      Set<Path> applicationFilePaths = new HashSet<>();
      if (project.srcMainConfiguration().toFile().exists()) {
        applicationFilePaths.addAll(getFiles(project.srcMainConfiguration()));
      }
      if (project.srcTestConfiguration().toFile().exists()) {
        applicationFilePaths.addAll(getFiles(project.srcTestConfiguration()));
      }

      Map<Path, Document> applicationDocuments = new HashMap<>();
      for (Path afp : applicationFilePaths) {
        try {
          applicationDocuments.put(afp, generateDocument(afp));
        } catch (JDOMException | IOException e) {
          throw new RuntimeException("Application Model Generation Error - Fail to parse file: " + afp);
        }
      }
      return new ApplicationModel(applicationDocuments);
    }

    private Document generateDocument(Path filePath) throws JDOMException, IOException {
      SAXBuilder saxBuilder = new SAXBuilder();
      return saxBuilder.build(filePath.toFile());
    }
  }
}
