/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.munit.steps;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * This steps migrates the MUnit Utils FTP Server
 * @author Mulesoft Inc.
 */
public class MUnitUtilsFTPServer extends AbstractApplicationModelMigrationStep {

  private static final String FTP_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/ftpserver";
  private static final String FTP_NAMESPACE_PREFIX = "ftpserver";
  private static final Namespace FTP_NAMESPACE = Namespace.getNamespace(FTP_NAMESPACE_PREFIX, FTP_NAMESPACE_URI);

  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + FTP_NAMESPACE_URI + "']";

  @Override
  public String getDescription() {
    return "Migrate MUnit FTP Utils processor.";
  }

  public MUnitUtilsFTPServer() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(FTP_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {

    if (element.getName().equals("start-server") || element.getName().equals("stop-server")) {
      report.report("munit.ftpServer", element, element.getParentElement());
      element.detach();
    } else if (element.getName().equals("config")) {

      Element connection = new Element("connection", FTP_NAMESPACE);
      copyAttributeIfPresent(element, connection, "username");
      copyAttributeIfPresent(element, connection, "password");
      copyAttributeIfPresent(element, connection, "homeDir");
      copyAttributeIfPresent(element, connection, "secure");
      copyAttributeIfPresent(element, connection, "anonymous");

      element.addContent(connection);
    }
  }

}
