/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import org.jdom2.Element;
import org.jdom2.Namespace;

import com.mulesoft.tools.migration.engine.step.AbstractApplicationModelMigrationStep;

/**
 * Migrates the configuration of the TCP/UDP Transport
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SocketsConfig extends AbstractApplicationModelMigrationStep {

  private static final String TCP_NAMESPACE = "http://www.mulesoft.org/schema/mule/tcp";
  private static final String SOCKETS_NAMESPACE = "http://www.mulesoft.org/schema/mule/sockets";

  public static final String XPATH_SELECTOR = ""
      + "//*[namespace-uri()='" + TCP_NAMESPACE + "']";

  @Override
  public String getDescription() {
    return "Update Sockets config.";
  }

  public SocketsConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object) throws RuntimeException {
    final Namespace socketsNamespace = Namespace.getNamespace("sockets", SOCKETS_NAMESPACE);
    object.setNamespace(socketsNamespace);

    if ("client-socket-properties".equals(object.getName())) {
      object.setNamespace(socketsNamespace);

      //        c.getDocument().getRootElement().setAttribute("http://www.w3.org/2001/XMLSchema-instance", 
      //                                                      "xsi:schemaLocation", SOCKETS_NAMESPACE + " http://www.mulesoft.org/schema/mule/sockets/current/mule-sockets.xsd");
    }
  }

  protected void copyAttributeIfPresent(final Element source, final Element target, final String attributeName) {
    copyAttributeIfPresent(source, target, attributeName, attributeName);
  }

  protected void copyAttributeIfPresent(final Element source, final Element target, final String sourceAttributeName,
                                        final String targetAttributeName) {
    if (source.getAttribute(sourceAttributeName) != null) {
      target.setAttribute(targetAttributeName, source.getAttributeValue(sourceAttributeName));
      source.removeAttribute(sourceAttributeName);
    }
  }
}
