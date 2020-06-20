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
package com.mulesoft.tools.migration.library.mule.steps.security.crc32;

import static com.mulesoft.tools.migration.project.model.ApplicationModel.addNameSpace;
import static java.util.Collections.singletonList;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.Optional;

/**
 * Update crc32 calculate to use the cryptography module.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Crc32Calculate extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  private static final String CRC32_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/crc32";
  private static final Namespace CRC32_NAMESPACE = getNamespace("crc32", CRC32_NAMESPACE_URI);
  private static final Namespace CRYPTO_NAMESPACE = getNamespace("crypto", "http://www.mulesoft.org/schema/mule/crypto");

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri() = '" + CRC32_NAMESPACE_URI + "' and local-name() = 'calculate']";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update crc32 calculate to use the cryptography module.";
  }

  public Crc32Calculate() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(singletonList(CRC32_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    addCryptoNamespace(element.getDocument());

    element.setName("calculateChecksum");
    element.setNamespace(CRYPTO_NAMESPACE);

    final Optional<Element> config =
        getApplicationModel()
            .getNodeOptional("/*/*[namespace-uri() = '" + CRC32_NAMESPACE_URI + "' and local-name() = 'config']");
    element.removeAttribute("config-ref");

    element.setAttribute("algorithm", "CRC32");

    if (config.isPresent()) {
      element.setAttribute("target",
                           getExpressionMigrator().migrateExpression(config.get().getAttributeValue("targetExpression"), true,
                                                                     element, true));
    } else {
      element.setAttribute("target", "crc32");
    }
  }

  public static void addCryptoNamespace(Document document) {
    addNameSpace(CRYPTO_NAMESPACE, "http://www.mulesoft.org/schema/mule/crypto/current/mule-crypto.xsd",
                 document);
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }


  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }


}
