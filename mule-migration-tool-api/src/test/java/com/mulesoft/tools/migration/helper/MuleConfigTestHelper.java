/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.helper;

public final class MuleConfigTestHelper {

  private MuleConfigTestHelper() {
    // Nothing to do
  }

  public static String emptyMuleConfig() {
    return "" +
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "\n" +
        "<mule xmlns=\"http://www.mulesoft.org/schema/mule/core\"\n" +
        "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "      xsi:schemaLocation=\"http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd\">\n"
        +
        "\n" +
        "</mule>";
  }

  public static String emptyMuleDomainConfig() {
    return "" +
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "\n" +
        "<mule-domain xmlns=\"http://www.mulesoft.org/schema/mule/domain\"\n" +
        "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "      xsi:schemaLocation=\"http://www.mulesoft.org/schema/mule/domain http://www.mulesoft.org/schema/mule/domain/current/mule-domain.xsd\">\n"
        +
        "\n" +
        "</mule-domain>";
  }

}
