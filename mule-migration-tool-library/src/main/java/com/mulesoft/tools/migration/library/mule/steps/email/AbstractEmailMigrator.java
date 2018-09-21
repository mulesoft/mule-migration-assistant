/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.email;

import static java.util.Optional.of;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.Optional;


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
