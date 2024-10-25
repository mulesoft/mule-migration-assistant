/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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
