/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
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
