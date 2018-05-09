/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.file;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getElementsFromDocument;
import static com.mulesoft.tools.migration.xml.AdditionalNamespaces.FILE;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.ExpressionMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.ArrayList;
import java.util.List;

/**
 * Migrates the global endpoints of the file transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FileGlobalEndpoint extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = "/mule:mule/file:endpoint";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update File global endpoints.";
  }

  public FileGlobalEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Namespace fileNs = Namespace.getNamespace(FILE.prefix(), FILE.uri());

    List<Element> inboundRefsToGlobal = new ArrayList<>();
    inboundRefsToGlobal.addAll(getElementsFromDocument(object.getDocument(), "/mule:mule/mule:flow/mule:inbound-endpoint[@ref='"
        + object.getAttributeValue("name") + "']"));
    inboundRefsToGlobal.addAll(getElementsFromDocument(object.getDocument(), "/mule:mule/mule:flow/file:inbound-endpoint[@ref='"
        + object.getAttributeValue("name") + "']"));
    inboundRefsToGlobal.addAll(getElementsFromDocument(object.getDocument(), "/mule:mule/mule:flow/file:endpoint[@ref='"
        + object.getAttributeValue("name") + "']"));

    for (Element referent : inboundRefsToGlobal) {
      referent.setNamespace(fileNs);
      referent.setName("inbound-endpoint");

      for (Attribute attribute : object.getAttributes()) {
        if (!"name".equals(attribute.getName()) && referent.getAttribute(attribute.getName()) == null) {
          referent.setAttribute(attribute.getName(), attribute.getValue());
        }
      }

      referent.addContent(object.removeContent());

      referent.removeAttribute("ref");
    }

    List<Element> outboundRefsToGlobal = new ArrayList<>();
    outboundRefsToGlobal.addAll(getElementsFromDocument(object.getDocument(), "/mule:mule/mule:flow/mule:outbound-endpoint[@ref='"
        + object.getAttributeValue("name") + "']"));
    outboundRefsToGlobal.addAll(getElementsFromDocument(object.getDocument(), "/mule:mule/mule:flow/file:outbound-endpoint[@ref='"
        + object.getAttributeValue("name") + "']"));
    outboundRefsToGlobal.addAll(getElementsFromDocument(object.getDocument(), "/mule:mule/mule:flow/file:endpoint[@ref='"
        + object.getAttributeValue("name") + "']"));

    for (Element referent : outboundRefsToGlobal) {
      referent.setNamespace(fileNs);
      referent.setName("outbound-endpoint");

      for (Attribute attribute : object.getAttributes()) {
        if (!"name".equals(attribute.getName()) && referent.getAttribute(attribute.getName()) == null) {
          referent.setAttribute(attribute.getName(), attribute.getValue());
        }
      }

      referent.addContent(object.removeContent());

      referent.removeAttribute("ref");
    }

    object.getParent().removeContent(object);
  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }

}
