/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.utils;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.ApplicationModel.ApplicationModelBuilder;
import com.mulesoft.tools.migration.project.structure.mule.three.MuleThreeApplication;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public class ApplicationModelUtils {

  public static ApplicationModel generateAppModel(List<URL> applicationDocuments, Path projectPath) throws Exception {
    MuleThreeApplication muleApp = new MuleThreeApplication(buildProject(applicationDocuments, projectPath));
    return new ApplicationModelBuilder(muleApp).build();
  }

  private static Path buildProject(List<URL> applicationDocuments, Path projectPath) throws IOException {
    File app = projectPath.resolve("src").resolve("main").resolve("app").toFile();
    app.mkdirs();

    for (URL documentPath : applicationDocuments) {
      FileUtils.copyURLToFile(documentPath, new File(app, documentPath.getFile()));
    }
    return projectPath;
  }
}
