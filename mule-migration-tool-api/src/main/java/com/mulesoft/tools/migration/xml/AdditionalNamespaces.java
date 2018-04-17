/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.xml;

/**
 * All supported namespaces for the migration tool
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public enum AdditionalNamespaces {

  HTTP("http", "http://www.mulesoft.org/schema/mule/http"),

  MUNIT("munit", "http://www.mulesoft.org/schema/mule/munit"),

  TLS("tls", "http://www.mulesoft.org/schema/mule/tls"),

  COMPATIBILITY("compatibility", "http://www.mulesoft.org/schema/mule/compatibility"),

  TCP("tcp", "http://www.mulesoft.org/schema/mule/tcp"),

  SOCKETS("sockets", "http://www.mulesoft.org/schema/mule/sockets"),

  CORE("mule", "http://www.mulesoft.org/schema/mule/core"),

  JSON("json", "http://www.mulesoft.org/schema/mule/json"),

  MOCK("mock", "http://www.mulesoft.org/schema/mule/mock");

  private String prefix;
  private String uri;

  AdditionalNamespaces(String prefix, String uri) {
    this.prefix = prefix;
    this.uri = uri;
  }

  public String uri() {
    return this.uri;
  }

  public String prefix() {
    return this.prefix;
  }

}
