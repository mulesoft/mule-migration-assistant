/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;

import com.mulesoft.tools.migration.step.AbstractGlobalEndpointMigratorStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.HashSet;
import java.util.Set;

/**
 * Migrates the global endpoints of the file transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class GenericGlobalEndpoint extends AbstractGlobalEndpointMigratorStep {

  public static final String XPATH_SELECTOR = "/*/mule:endpoint";

  @Override
  public String getDescription() {
    return "Update generic global endpoints.";
  }

  public GenericGlobalEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    doExecute(object, report);
  }

  @Override
  protected Namespace getNamespace() {
    return Namespace.getNamespace("mule", CORE_NAMESPACE.getURI());
  }

  @Override
  protected void changeNamespace(Element referent) {
    // Nothing to do
  }

  @Override
  protected Set<Element> getRefs(Element object) {
    Set<Element> refsToGlobal = new HashSet<>();
    refsToGlobal
        .addAll(getApplicationModel().getNodes("/*/mule:flow//*[local-name() = 'endpoint' and @ref='"
            + object.getAttributeValue("name") + "']"));
    return refsToGlobal;
  }

  @Override
  protected Set<Element> getInboundRefs(Element object) {
    Set<Element> inboundRefsToGlobal = new HashSet<>();
    inboundRefsToGlobal
        .addAll(getApplicationModel().getNodes("/*/mule:flow/*[local-name() = 'inbound-endpoint' and @ref='"
            + object.getAttributeValue("name") + "']"));
    return inboundRefsToGlobal;
  }

  @Override
  protected Set<Element> getOutboundRefs(Element object) {
    Set<Element> outboundRefsToGlobal = new HashSet<>();
    outboundRefsToGlobal.addAll(getApplicationModel()
        .getNodes("/*/mule:flow//*[local-name() = 'outbound-endpoint' and @ref='"
            + object.getAttributeValue("name") + "']"));
    return outboundRefsToGlobal;
  }
}
