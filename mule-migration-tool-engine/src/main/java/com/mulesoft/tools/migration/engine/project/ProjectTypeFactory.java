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
package com.mulesoft.tools.migration.engine.project;

import static com.mulesoft.tools.migration.project.ProjectType.BASIC;
import static com.mulesoft.tools.migration.project.ProjectType.JAVA;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_DOMAIN;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_THREE_APPLICATION;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_THREE_DOMAIN;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_THREE_MAVEN_APPLICATION;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_THREE_MAVEN_DOMAIN;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_THREE_POLICY;
import static java.nio.file.Files.exists;
import static org.apache.commons.io.FileUtils.listFiles;

import com.mulesoft.tools.migration.engine.project.structure.JavaProject;
import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourApplication;
import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourDomain;
import com.mulesoft.tools.migration.engine.project.structure.mule.three.MuleThreeApplication;
import com.mulesoft.tools.migration.engine.project.structure.mule.three.MuleThreeDomain;
import com.mulesoft.tools.migration.engine.project.structure.mule.three.MuleThreeMavenApplication;
import com.mulesoft.tools.migration.engine.project.structure.mule.three.MuleThreeMavenDomain;
import com.mulesoft.tools.migration.engine.project.structure.mule.three.MuleThreePolicy;
import com.mulesoft.tools.migration.project.ProjectType;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * MuleFourApplication It gets the project type based on the project path
 *
 * @author Mulesoft Inc.
 */
public class ProjectTypeFactory {

  private DocumentBuilder db;

  public ProjectTypeFactory() {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(false);
    dbf.setValidating(false);

    try {
      db = dbf.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  public ProjectType getProjectType(Path projectPath) throws Exception {
    if (exists(projectPath.resolve(MuleThreeMavenApplication.srcMainConfigurationPath)) &&
        exists(projectPath.resolve("pom.xml"))) {
      return MULE_THREE_MAVEN_APPLICATION;
    } else if (exists(projectPath.resolve(MuleThreeMavenDomain.srcMainConfigurationPath)) &&
        exists(projectPath.resolve("pom.xml"))) {
      return MULE_THREE_MAVEN_DOMAIN;
    } else if (exists(projectPath.resolve(MuleThreeApplication.srcMainConfigurationPath))
        && listFiles(projectPath.resolve(MuleThreeApplication.srcMainConfigurationPath).toFile(), new String[] {"xml"}, false)
            .stream().filter(f -> rootElement(f, "mule"))
            .count() > 0) {
      return MULE_THREE_APPLICATION;
    } else if (exists(projectPath.resolve(MuleThreeDomain.srcMainConfigurationPath))
        && listFiles(projectPath.resolve(MuleThreeDomain.srcMainConfigurationPath).toFile(), new String[] {"xml"}, false).stream()
            .filter(f -> rootElement(f, "domain:mule-domain"))
            .count() > 0) {
      return MULE_THREE_DOMAIN;
    } else if (MuleThreePolicy.isPolicyInFolder(projectPath)) {
      return MULE_THREE_POLICY;
    } else if (exists(projectPath.resolve(MuleFourApplication.srcMainConfigurationPath))) {
      return MULE_FOUR_APPLICATION;
    } else if (exists(projectPath.resolve(MuleFourDomain.srcMainConfigurationPath))) {
      return MULE_FOUR_DOMAIN;
    } else if (exists(projectPath.resolve(JavaProject.srcMainJavaPath))) {
      return JAVA;
    } else {
      return BASIC;
    }
  }


  private boolean rootElement(File configFile, String expectedRootTag) {
    Document doc;
    try {
      doc = db.parse(configFile);
    } catch (SAXException | IOException e) {
      throw new RuntimeException(e);
    }

    return expectedRootTag.equals(doc.getDocumentElement().getTagName());
  }

}
