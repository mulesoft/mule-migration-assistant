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
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;

/**
 * Migrate Property Placeholder component.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class PropertyPlaceholder extends AbstractApplicationModelMigrationStep {

  private static final String SPRING_CONTEXT_NS_PREFIX = "context";
  private static final String SPRING_CONTEXT_NS_URI = "http://www.springframework.org/schema/context";
  private static final String SPRING_CONTEXT_SCHEMA = "http://www.springframework.org/schema/context/spring-context.xsd";
  public static final String XPATH_SELECTOR =
      "//*[namespace-uri() = '" + SPRING_CONTEXT_NS_URI + "' and local-name()='property-placeholder']";

  @Override
  public String getDescription() {
    return "Migrate Property Placeholder component";
  }

  public PropertyPlaceholder() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {

    Attribute propsLocation = element.getAttribute("location");
    if (propsLocation != null) {
      Element configProperties = new Element("configuration-properties", CORE_NAMESPACE);
      configProperties.setAttribute("file", propsLocation.getValue());

      addTopLevelElement(configProperties, element.getDocument());
    }

    getApplicationModel().removeNameSpace(Namespace.getNamespace(SPRING_CONTEXT_NS_PREFIX, SPRING_CONTEXT_NS_URI),
                                          SPRING_CONTEXT_SCHEMA, element.getDocument());
    element.detach();
  }
}
