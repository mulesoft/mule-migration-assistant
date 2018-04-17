/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.xml;

import org.jdom2.Namespace;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.mulesoft.tools.migration.xml.AdditionalNamespacesFactory.getAdditionalNamespaces;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

public class AdditionalNamespacesFactoryTest {

  private List<Namespace> namespaces = new ArrayList<>();

  @Test
  public void getAdditionalNamespacesTest() throws Exception {
    namespaces = getAdditionalNamespaces();
    assertThat("The list generated is empty", namespaces.size(), is(greaterThan(0)));
    assertThat("The namespace doesn't match", namespaces.get(0).getPrefix(), is("http"));
  }
}
