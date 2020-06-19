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
package com.mulesoft.tools.migration.library.munit.steps;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.NamespaceContribution;

import org.jdom2.Document;
import org.jdom2.Namespace;

/**
 * This steps migrates the MUnit 1.x assert-true
 *
 * @author Mulesoft Inc.
 */
public class MUnitNamespaces implements NamespaceContribution {

  private static final String MUNIT_PATH = "src/test/munit";
  private static final String MUNIT_MOCK_NAME = "mock";
  private static final String MUNIT_MOCK_URI = "http://www.mulesoft.org/schema/mule/mock";
  private static final String MUNIT_MOCK_SCHEMA = "http://www.mulesoft.org/schema/mule/mock/current/mule-mock.xsd";
  private static final String MUNIT_TOOLS_NAME = "munit-tools";
  private static final String MUNIT_TOOLS_URI = "http://www.mulesoft.org/schema/mule/munit-tools";
  private static final String MUNIT_TOOLS_SCHEMA = "http://www.mulesoft.org/schema/mule/munit-tools/current/mule-munit-tools.xsd";

  @Override
  public String getDescription() {
    return "Remove MUnit Mock namespace and add MUnit-Tools default namespace.";
  }

  @Override
  public void execute(ApplicationModel applicationModel, MigrationReport report) throws RuntimeException {
    try {
      applicationModel.removeNameSpace(MUNIT_MOCK_NAME, MUNIT_MOCK_URI, MUNIT_MOCK_SCHEMA);

      Namespace namespace = Namespace.getNamespace(MUNIT_TOOLS_NAME, MUNIT_TOOLS_URI);
      applicationModel.getApplicationDocuments().values().stream().filter(d -> isMUnitFile(d))
          .forEach(e -> applicationModel.addNameSpace(namespace, MUNIT_TOOLS_SCHEMA, e));
    } catch (Exception e) {
      throw new MigrationStepException("Fail to apply step. " + e.getMessage(), e);
    }
  }

  public boolean isMUnitFile(Document document) {
    if (document.getBaseURI() != null && document.getBaseURI().contains(MUNIT_PATH)) {
      return true;
    } else {
      return false;
    }
  }
}
