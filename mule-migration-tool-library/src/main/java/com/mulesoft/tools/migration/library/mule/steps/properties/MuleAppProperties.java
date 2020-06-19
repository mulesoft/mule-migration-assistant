/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
      Properties properties = new Properties();
      properties.load(new FileInputStream(muleAppProperties));
      properties.forEach((k, v) -> appProperties.add((String) k));
    }
    return appProperties;
  }
}
