/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.security.crc32;

import static com.mulesoft.tools.migration.project.model.ApplicationModel.addNameSpace;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateExpression;
import static java.util.Collections.singletonList;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.library.mule.steps.core.filter.AbstractFilterMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Update crc32 filter to use the cryptography module.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Crc32Filter extends AbstractFilterMigrator {

  private static final String CRC32_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/crc32";
  private static final Namespace CRC32_NAMESPACE = getNamespace("crc32", CRC32_NAMESPACE_URI);
  private static final Namespace CRYPTO_NAMESPACE = getNamespace("crypto", "http://www.mulesoft.org/schema/mule/crypto");

  public static final String XPATH_SELECTOR = "//*[namespace-uri() = '" + CRC32_NAMESPACE_URI + "' and local-name() = 'filter']";

  @Override
  public String getDescription() {
    return "Update crc32 filter to use the cryptography module.";
  }

  public Crc32Filter() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(singletonList(CRC32_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    addCryptoNamespace(element.getDocument());

    element.setName("validate-checksum");
    element.setNamespace(CRYPTO_NAMESPACE);

    element.removeAttribute("config-ref");

    element.setAttribute("algorithm", "CRC32");

    migrateExpression(element.getAttribute("expectedChecksum"), getExpressionMigrator());
    element.getAttribute("expectedChecksum").setName("expected");

    handleFilter(element);
  }

  public static void addCryptoNamespace(Document document) {
    addNameSpace(CRYPTO_NAMESPACE, "http://www.mulesoft.org/schema/mule/crypto/current/mule-crypto.xsd",
                 document);
  }

}
