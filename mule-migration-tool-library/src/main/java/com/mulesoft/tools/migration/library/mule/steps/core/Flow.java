/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Adds compatibility stuff to the flow
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Flow extends AbstractApplicationModelMigrationStep {

  private static final String CORE_NAMESPACE = "http://www.mulesoft.org/schema/mule/core";
  private static final String COMPATIBILITY_NAMESPACE = "http://www.mulesoft.org/schema/mule/compatibility";

  public static final String XPATH_SELECTOR = "//*[local-name()='flow']";

  @Override
  public String getDescription() {
    return "Copy outbound properties to a variable so they are available in DW expressions in the listener.";
  }

  public Flow() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    getApplicationModel().addNameSpace(Namespace.getNamespace("compatibility", COMPATIBILITY_NAMESPACE),
                                       "http://www.mulesoft.org/schema/mule/compatibility/current/mule-compatibility.xsd",
                                       element.getDocument());

    Element setVariable = new Element("set-variable", Namespace.getNamespace(CORE_NAMESPACE));
    setVariable.setAttribute("variableName", "compatibility_outboundProperties");
    setVariable.setAttribute("value", "#[mel:message.outboundProperties]");
    element.addContent(setVariable);

    report.report(WARN, setVariable, setVariable,
                  "Instead of setting outbound properties in the flow, its values must be set explicitly in the operation/listener.",
                  "https://docs.mulesoft.com/mule-user-guide/v/4.1/intro-mule-message#outbound-properties");
  }


}
