/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.step;

import static java.util.stream.Collectors.toList;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.HashSet;
import java.util.List;
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
    List<Content> children = object.removeContent();

    for (Element referent : getRefs(object)) {
      changeNamespace(referent);

      copyAttributes(object, referent);

      referent.addContent(children.stream().map(e -> e.clone()).collect(toList()));

      referent.getAttribute("ref").setName("name");
    }

    for (Element referent : getInboundRefs(object)) {
      changeNamespace(referent);

      copyAttributes(object, referent);

      referent.addContent(children.stream().map(e -> e.clone()).collect(toList()));

      referent.getAttribute("ref").setName("name");
    }

    for (Element referent : getOutboundRefs(object)) {
      changeNamespace(referent);

      copyAttributes(object, referent);

      referent.addContent(children.stream().map(e -> e.clone()).collect(toList()));

      referent.getAttribute("ref").setName("name");
    }

    for (Element referent : getQuartzJobRefs(object)) {
      changeNamespace(referent);

      copyAttributes(object, referent);

      referent.addContent(children.stream().map(e -> e.clone()).collect(toList()));

      referent.getAttribute("ref").setName("name");
    }

    object.detach();
  }

  public static void copyAttributes(Element object, Element referent) {
    for (Attribute attribute : object.getAttributes()) {
      if (referent.getAttribute(attribute.getName(), attribute.getNamespace()) == null) {
        if (!"name".equals(attribute.getName()) && referent.getAttribute(attribute.getName()) == null) {
          referent.setAttribute(attribute.getName(), attribute.getValue(), attribute.getNamespace());
        }
      }
    }
  }

  protected Set<Element> getRefs(Element element) {
    Set<Element> refsToGlobal = new HashSet<>();
    refsToGlobal
        .addAll(getApplicationModel().getNodes("//" + getNamespace().getPrefix() + ":endpoint[@ref='"
            + element.getAttributeValue("name") + "']"));
    return refsToGlobal;
  }

  protected Set<Element> getInboundRefs(Element object) {
    Set<Element> inboundRefsToGlobal = new HashSet<>();
    inboundRefsToGlobal
        .addAll(getApplicationModel()
            .getNodes("/*/mule:flow/mule:inbound-endpoint[@ref='" + object.getAttributeValue("name") + "']"));
    inboundRefsToGlobal
        .addAll(getApplicationModel().getNodes("/*/mule:flow/" + getNamespace().getPrefix() + ":inbound-endpoint[@ref='"
            + object.getAttributeValue("name") + "']"));
    return inboundRefsToGlobal;
  }

  protected Set<Element> getOutboundRefs(Element object) {
    Set<Element> outboundRefsToGlobal = new HashSet<>();
    outboundRefsToGlobal.addAll(getApplicationModel()
        .getNodes("//mule:outbound-endpoint[@ref='" + object.getAttributeValue("name") + "']"));
    outboundRefsToGlobal.addAll(getApplicationModel()
        .getNodes("//" + getNamespace().getPrefix() + ":outbound-endpoint[@ref='"
            + object.getAttributeValue("name") + "']"));
    return outboundRefsToGlobal;
  }

  protected Set<Element> getQuartzJobRefs(Element object) {
    Set<Element> quartzdRefsToGlobal = new HashSet<>();
    quartzdRefsToGlobal.addAll(getApplicationModel()
        .getNodes("//quartz:job-endpoint[@ref='" + object.getAttributeValue("name") + "']"));
    return quartzdRefsToGlobal;
  }

  protected void changeNamespace(Element referent) {
    referent.setNamespace(getNamespace());
  }
}
