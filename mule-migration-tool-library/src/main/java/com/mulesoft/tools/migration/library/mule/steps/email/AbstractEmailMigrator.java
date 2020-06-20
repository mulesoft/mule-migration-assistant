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
package com.mulesoft.tools.migration.library.mule.steps.email;

import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static org.jdom2.Namespace.getNamespace;

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

  public static final String SMTP_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/smtp";
  public static final String SMTPS_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/smtps";
  public static final String IMAP_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/imap";
  public static final String IMAPS_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/imaps";
  public static final String POP3_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/pop3";
  public static final String POP3S_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/pop3s";

  public static final Namespace TLS_NAMESPACE = getNamespace("tls", "http://www.mulesoft.org/schema/mule/tls");

  public static final Namespace EMAIL_NAMESPACE = getNamespace("email", "http://www.mulesoft.org/schema/mule/email");
  public static final String EMAIL_SCHEMA_LOC = "http://www.mulesoft.org/schema/mule/email/current/mule-email.xsd";

  public AbstractEmailMigrator() {
    this.setNamespacesContributions(asList(EMAIL_NAMESPACE,
                                           getNamespace("smtp", SMTP_NAMESPACE_URI),
                                           getNamespace("smtps", SMTPS_NAMESPACE_URI),
                                           getNamespace("imap", IMAP_NAMESPACE_URI),
                                           getNamespace("imaps", IMAPS_NAMESPACE_URI),
                                           getNamespace("pop3", POP3_NAMESPACE_URI),
                                           getNamespace("pop3s", POP3S_NAMESPACE_URI)));
  }

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
