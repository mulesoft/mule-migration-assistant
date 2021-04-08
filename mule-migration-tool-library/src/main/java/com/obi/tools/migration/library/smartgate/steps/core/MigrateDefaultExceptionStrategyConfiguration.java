/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.steps.core;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.xpath.XPathFactory;

/**
 * Migrate defaultExceptionStrategy-ref components
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MigrateDefaultExceptionStrategyConfiguration extends AbstractApplicationModelMigrationStep {

  private static final String GLOBAL_ERROR_HANDLER_XML = "global-error-handler.xml";
  private static final String GLOBAL_ERROR_HANDLER = "global-error-handler";
  private static final String DEFAULT_ERROR_HANDLER_REF = "defaultErrorHandler-ref";
  private static final String DEFAULT_EXCEPTION_STRATEGY_REF = "defaultExceptionStrategy-ref";
  public static final String XPATH_SELECTOR = getCoreXPathSelector("configuration");


  @Override
  public String getDescription() {
    return "Migrate defaultExceptionStrategy-ref components";
  }

  public MigrateDefaultExceptionStrategyConfiguration() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    final Element parentElement = element.getParentElement();
    Attribute attribute = element.getAttribute(DEFAULT_EXCEPTION_STRATEGY_REF);
    if (attribute != null && attribute.getValue().equals("global-exception-strategy")) {
      element.removeAttribute(attribute);
      element.setAttribute(new Attribute(DEFAULT_ERROR_HANDLER_REF, GLOBAL_ERROR_HANDLER));
    }

    if (getApplicationModel()
        .getElementsFromDocument(XPathFactory.instance().compile("//*[@file = '" + GLOBAL_ERROR_HANDLER_XML + "']"),
                                 element.getDocument())
        .isEmpty()) {
      // <import doc:name="Import" file="global-error-handler.xml" />
      Element configProperties = new Element("import", CORE_NAMESPACE);
      configProperties.setAttribute("file", GLOBAL_ERROR_HANDLER_XML);

      parentElement.addContent(configProperties);
      // addTopLevelElement(configProperties, element.getDocument());
    }
  }
}
