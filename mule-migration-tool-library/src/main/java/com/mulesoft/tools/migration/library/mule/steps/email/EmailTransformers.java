/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.email;

import static com.mulesoft.tools.migration.library.mule.steps.email.AbstractEmailMigrator.EMAIL_NAMESPACE;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Remove the email transformers
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class EmailTransformers extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "//*[(namespace-uri() = '" + EMAIL_NAMESPACE.getURI() + "' and ("
      + "local-name() = 'email-to-string-transformer' or "
      + "local-name() = 'string-to-email-transformer' or "
      + "local-name() = 'object-to-mime-transformer' or "
      + "local-name() = 'mime-to-bytes-transformer' or "
      + "local-name() = 'bytes-to-mime-transformer'"
      + ")) or (namespace-uri() = 'http://www.mulesoft.org/schema/mule/core' and ("
      + "@class = 'org.mule.transport.email.transformers.EmailMessageToString' or "
      + "@class = 'org.mule.transport.email.transformers.StringToEmailMessage' or "
      + "@class = 'org.mule.transport.email.transformers.ObjectToMimeMessage' or "
      + "@class = 'org.mule.transport.email.transformers.MimeMessageToRfc822ByteArray' or "
      + "@class = 'org.mule.transport.email.transformers.Rfc822ByteArraytoMimeMessage'"
      + "))]";

  @Override
  public String getDescription() {
    return "Remove the email transformers.";
  }

  public EmailTransformers() {
    this.setAppliedTo(XPATH_SELECTOR);

  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    report.report("email.transformers", object, object.getParentElement());

    for (Element ref : getApplicationModel().getNodes("//*[@ref = '" + object.getAttributeValue("name") + "']")) {
      ref.detach();
    }

    object.detach();
  }
}
