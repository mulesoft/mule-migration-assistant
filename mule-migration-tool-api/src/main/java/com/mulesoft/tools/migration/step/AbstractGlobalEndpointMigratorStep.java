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
      if (!"name".equals(attribute.getName()) && referent.getAttribute(attribute.getName()) == null) {
        referent.setAttribute(attribute.getName(), attribute.getValue());
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
