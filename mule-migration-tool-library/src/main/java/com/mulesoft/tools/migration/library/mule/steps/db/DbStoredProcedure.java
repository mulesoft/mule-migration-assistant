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
package com.mulesoft.tools.migration.library.mule.steps.db;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateOperationStructure;
import static java.util.stream.Collectors.toList;

import com.mulesoft.tools.migration.library.tools.mel.DefaultMelCompatibilityResolver;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

import java.util.List;

/**
 * Migrates the call stored procedure operation of the DB Connector
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class DbStoredProcedure extends AbstractDbOperationMigrator {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri() = '" + DB_NAMESPACE_URI + "' and local-name() = 'stored-procedure']";

  @Override
  public String getDescription() {
    return "Update call stored procedure operation of the DB Connector.";
  }

  public DbStoredProcedure() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    migrateSql(object);
    migrateInputParamTypes(object);
    migrateInputParams(object);

    List<Element> outParams = object.getChildren("out-param", DB_NAMESPACE).stream()
        .map(ip -> new Element("output-parameter", DB_NAMESPACE)
            .setAttribute("key", ip.getAttributeValue("name")))
        .collect(toList());
    if (!outParams.isEmpty()) {
      object.addContent(new Element("output-parameters", DB_NAMESPACE).addContent(outParams));
    }
    object.removeChildren("out-param", DB_NAMESPACE);

    List<Element> inoutParams = object.getChildren("inout-param", DB_NAMESPACE).stream()
        .map(ip -> new Element("in-out-parameter", DB_NAMESPACE)
            .setAttribute("key", ip.getAttributeValue("name"))
            .setAttribute("value", ip.getAttributeValue("value")))
        .collect(toList());
    if (!inoutParams.isEmpty()) {
      object.addContent(new Element("in-out-parameters", DB_NAMESPACE).addContent(inoutParams));
    }
    object.removeChildren("inout-param", DB_NAMESPACE);

    if (object.getAttribute("streaming") == null || "false".equals(object.getAttributeValue("streaming"))) {
      report.report("db.streaming", object, object);
    }
    object.removeAttribute("streaming");

    if (object.getAttribute("source") != null) {
      report.report("db.source", object, object);
      object.removeAttribute("source");
    }

    migrateOperationStructure(getApplicationModel(), object, report, false, getExpressionMigrator(),
                              new DefaultMelCompatibilityResolver());
  }


}
