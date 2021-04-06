/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.steps.core;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.xpath.XPathFactory;

import java.io.File;

/**
 * Remove mule-app.properties add globalproperty and errorhandler import
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class PostGlobalsMigrations extends AbstractApplicationModelMigrationStep {

  private static final String GLOBAL_ERROR_HANDLER_XML = "global-error-handler.xml";
  private static final String FILE = "file";
  private static final String MULE_APP_FILE_NAME = "mule-app.properties";
  private static final String MULE_APP_FILE_PATH =
      "src" + File.separator + "main" + File.separator + "resources" + File.separator + MULE_APP_FILE_NAME;
  public static final String XPATH_SELECTOR = getCoreXPathSelector("configuration-properties");


  @Override
  public String getDescription() {
    return "Remove mule-app.properties add globalproperty and errorhandler import";
  }

  public PostGlobalsMigrations() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {

    final Element parentElement = element.getParentElement();
    final Attribute attribute = element.getAttribute(FILE);
    if (attribute != null && attribute.getValue().equals(MULE_APP_FILE_NAME)) {
      attribute.setValue("${mule.env}.properties");
    }
    final ApplicationModel applicationModel = getApplicationModel();

    File muleAppProperties = new File(applicationModel.getProjectBasePath().toFile(), MULE_APP_FILE_PATH);
    muleAppProperties.delete();

    // add Toplevel
    // <global-property name="mule.env" value="local"/>
    // <global-property name="encryptionKey" value="1234567890123456"/>

    Element globalPropertyMuleENV = new Element("global-property", CORE_NAMESPACE);
    globalPropertyMuleENV.setAttribute("name",
                                       "mule.env");
    globalPropertyMuleENV.setAttribute("value", "local");
    parentElement.addContent(globalPropertyMuleENV);
    //
    //
    Element globalPropertyEencryptionKey = new Element("global-property", CORE_NAMESPACE);
    globalPropertyEencryptionKey.setAttribute("name", "encryptionKey");
    globalPropertyEencryptionKey.setAttribute("value",
                                              "1234567890123456");
    parentElement.addContent(globalPropertyEencryptionKey);

    report.report("smartgate.globalProperty", globalPropertyEencryptionKey, globalPropertyEencryptionKey);

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
