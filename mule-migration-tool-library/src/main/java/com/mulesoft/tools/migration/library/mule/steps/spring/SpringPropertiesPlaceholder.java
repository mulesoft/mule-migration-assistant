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
package com.mulesoft.tools.migration.library.mule.steps.spring;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.util.version.VersionUtils.isVersionGreaterOrEquals;
import static java.lang.Boolean.parseBoolean;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrates the spring property placeholders.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SpringPropertiesPlaceholder extends AbstractSpringMigratorStep {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri()='http://www.springframework.org/schema/context' and local-name()='property-placeholder']";

  @Override
  public String getDescription() {
    return "Migrates the spring property placeholders.";
  }

  public SpringPropertiesPlaceholder() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    int idx = 1;
    for (String location : object.getAttributeValue("location").split("\\,")) {
      Element confProp = new Element("configuration-properties", CORE_NAMESPACE);
      confProp.setAttribute("file", location);

      if (object.getAttribute("file-encoding") != null) {
        if (isVersionGreaterOrEquals(getApplicationModel().getMuleVersion(), "4.2.0")) {
          confProp.setAttribute("encoding", object.getAttributeValue("file-encoding"));
        } else {
          report.report("configProperties.encoding", object, object);
        }
      }

      object.getDocument().getRootElement().addContent(idx, confProp);
    }

    if (object.getAttribute("order") != null) {
      report.report("configProperties.order", object, object);
    }
    if (object.getAttributeValue("properties-ref") != null
        || parseBoolean(object.getAttributeValue("ignore-resource-not-found", "false"))
        || parseBoolean(object.getAttributeValue("ignore-unresolvable", "false"))
        || parseBoolean(object.getAttributeValue("local-override", "false"))
        || parseBoolean(object.getAttributeValue("system-properties-mode", "false"))) {
      report.report("configProperties.springAttributes", object, object);
    }

    object.detach();
  }

}
