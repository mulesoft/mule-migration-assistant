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
          xmlRootElement = generateDocument(policyXml.toPath()).getRootElement();
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
