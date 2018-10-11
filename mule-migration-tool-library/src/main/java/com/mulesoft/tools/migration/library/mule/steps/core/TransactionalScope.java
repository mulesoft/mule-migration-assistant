/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeAttribute;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_EE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.util.XmlDslUtils;

import org.jdom2.Element;

/**
 * Migrate Transactional scope to the new Try component
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class TransactionalScope extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "//*["
      + "(namespace-uri()='" + CORE_NAMESPACE.getURI() + "' and local-name()='transactional') or"
      + " (namespace-uri()='" + CORE_EE_NAMESPACE.getURI()
      + "' and (local-name()='xa-transactional' or local-name()='multi-transactional'))]";

  @Override
  public String getDescription() {
    return "Update Transactional Scope to Try component.";
  }

  public TransactionalScope() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    try {
      final boolean xa = element != null && element.getName().equals("xa-transactional");
      final Element transformed = changeNodeName("", "try")
          .andThen(changeAttribute("action", of("transactionalAction"), empty()))
          .apply(element);
      if (xa) {
        transformed.setAttribute("transactionType", "XA");
      }
    } catch (Exception ex) {
      throw new MigrationStepException("Failed to migrate transactional scope.");
    }
  }
}
