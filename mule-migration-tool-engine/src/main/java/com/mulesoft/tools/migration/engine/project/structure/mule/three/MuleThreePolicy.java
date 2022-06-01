/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.engine.project.structure.mule.three;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.generateDocument;
import static java.util.Arrays.stream;

import com.mulesoft.tools.migration.engine.project.structure.mule.MuleProject;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Represents a mule three policy project structure
 *
 * @author Mulesoft Inc.
 */
public class MuleThreePolicy extends MuleProject {

  private static final Logger LOGGER = LoggerFactory.getLogger(MuleThreePolicy.class);

  public static boolean isPolicyInFolder(Path projectPath) {
    return stream(projectPath.toFile().listFiles((FilenameFilter) new SuffixFileFilter(".yaml"))).anyMatch(yaml -> {
      // Although it's common that the yaml and xml have the same name, it is not a requirement
      return stream(projectPath.toFile().listFiles((FilenameFilter) new SuffixFileFilter(".xml"))).anyMatch(policyXml -> {
        Element xmlRootElement;
        try {
          xmlRootElement = generateDocument(policyXml.toPath(), false).getRootElement();
        } catch (JDOMException | IOException e) {
          LOGGER.warn(e.getClass().getName() + ": " + e.getMessage());
          return false;
        }
        return "policy".equals(xmlRootElement.getName())
            && "http://www.mulesoft.org/schema/mule/policy".equals(xmlRootElement.getNamespaceURI());
      });
    });

  }

  public MuleThreePolicy(Path baseFolder) {
    super(baseFolder);
  }

  @Override
  public Path srcMainConfiguration() {
    return baseFolder.resolve(".");
  }

  @Override
  public Path srcTestConfiguration() {
    return baseFolder.resolve("./test");
  }
}
