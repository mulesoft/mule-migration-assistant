/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.step;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.HashSet;
import java.util.Set;

/**
 * Migrates the global endpoints to either inbound or outbound endpoints inside a flow
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractGlobalEndpointMigratorStep extends AbstractApplicationModelMigrationStep {

  protected abstract Namespace getNamespace();

  protected final void doExecute(Element object, MigrationReport report) {
    for (Element referent : getRefs(object)) {
      changeNamespace(referent);
      referent.setName("endpoint");

      for (Attribute attribute : object.getAttributes()) {
        if (referent.getAttribute(attribute.getName()) == null) {
          referent.setAttribute(attribute.getName(), attribute.getValue());
        }
      }

      referent.addContent(object.removeContent());

      referent.setAttribute("name", referent.getAttributeValue("ref"));
      referent.removeAttribute("ref");
    }

    for (Element referent : getInboundRefs(object)) {
      changeNamespace(referent);
      referent.setName("inbound-endpoint");

      for (Attribute attribute : object.getAttributes()) {
        if (referent.getAttribute(attribute.getName()) == null) {
          referent.setAttribute(attribute.getName(), attribute.getValue());
        }
      }

      referent.addContent(object.removeContent());

      referent.setAttribute("name", referent.getAttributeValue("ref"));
      referent.removeAttribute("ref");
    }

    for (Element referent : getOutboundRefs(object)) {
      changeNamespace(referent);
      referent.setName("outbound-endpoint");

      for (Attribute attribute : object.getAttributes()) {
        if (!"name".equals(attribute.getName()) && referent.getAttribute(attribute.getName()) == null) {
          referent.setAttribute(attribute.getName(), attribute.getValue());
        }
      }

      referent.addContent(object.removeContent());

      referent.setAttribute("name", referent.getAttributeValue("ref"));
      referent.removeAttribute("ref");
    }

    object.getParent().removeContent(object);
  }

  protected Set<Element> getRefs(Element object) {
    Set<Element> refsToGlobal = new HashSet<>();
    refsToGlobal
        .addAll(getApplicationModel().getNodes("/mule:mule/mule:flow/" + getNamespace().getPrefix() + ":endpoint[@ref='"
            + object.getAttributeValue("name") + "']"));
    return refsToGlobal;
  }

  protected Set<Element> getInboundRefs(Element object) {
    Set<Element> inboundRefsToGlobal = new HashSet<>();
    inboundRefsToGlobal
        .addAll(getApplicationModel()
            .getNodes("/mule:mule/mule:flow/mule:inbound-endpoint[@ref='" + object.getAttributeValue("name") + "']"));
    inboundRefsToGlobal
        .addAll(getApplicationModel().getNodes("/mule:mule/mule:flow/" + getNamespace().getPrefix() + ":inbound-endpoint[@ref='"
            + object.getAttributeValue("name") + "']"));
    return inboundRefsToGlobal;
  }

  protected Set<Element> getOutboundRefs(Element object) {
    Set<Element> outboundRefsToGlobal = new HashSet<>();
    outboundRefsToGlobal.addAll(getApplicationModel()
        .getNodes("/mule:mule/mule:flow/mule:outbound-endpoint[@ref='" + object.getAttributeValue("name") + "']"));
    outboundRefsToGlobal.addAll(getApplicationModel()
        .getNodes("/mule:mule/mule:flow/" + getNamespace().getPrefix() + ":outbound-endpoint[@ref='"
            + object.getAttributeValue("name") + "']"));
    return outboundRefsToGlobal;
  }

  protected void changeNamespace(Element referent) {
    referent.setNamespace(getNamespace());
  }
}
