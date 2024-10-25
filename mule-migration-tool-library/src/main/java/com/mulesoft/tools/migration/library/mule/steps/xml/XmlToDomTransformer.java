/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.xml;

import static com.google.common.collect.Lists.newArrayList;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the usage of XML To Dom Transformer
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class XmlToDomTransformer extends AbstractApplicationModelMigrationStep {

  private static final String MULEXML_NAMESPACE_PREFIX = "mulexml";
  public static final String MULEXML_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/xml";
  public static final Namespace MULEXML_NAMESPACE = Namespace.getNamespace(MULEXML_NAMESPACE_PREFIX, MULEXML_NAMESPACE_URI);

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri()='" + MULEXML_NAMESPACE_URI + "' and local-name()='xml-to-dom-transformer']";


  public XmlToDomTransformer() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(MULEXML_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Element parentElement = object.getParentElement();
    object.detach();
    report.report("mulexml.xmlToDom", object, parentElement);
  }
}
