/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.email;

import static java.util.Optional.of;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.Optional;

/**
 * Support for migrating elements of the email connector
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractEmailMigrator extends AbstractApplicationModelMigrationStep {

  public static final Namespace EMAIL_NAMESPACE = Namespace.getNamespace("email", "http://www.mulesoft.org/schema/mule/email");

  protected Optional<Element> resolveConnector(Element object, ApplicationModel appModel) {
    Optional<Element> connector;
    if (object.getAttribute("connector-ref") != null) {
      connector = of(getConnector(object.getAttributeValue("connector-ref")));
      object.removeAttribute("connector-ref");
    } else {
      connector = getDefaultConnector();
    }
    return connector;
  }

  protected abstract Element getConnector(String connectorName);

  protected abstract Optional<Element> getDefaultConnector();

}
