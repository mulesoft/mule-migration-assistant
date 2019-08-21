/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
    object.detach();
    report.report("mulexml.xmlToDom", object, object.getParentElement());
  }
}
