/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.xml;

import org.jdom2.Document;
import org.jdom2.Namespace;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.xml.AdditionalNamespacesFactory.getAdditionalNamespaces;
import static com.mulesoft.tools.migration.xml.AdditionalNamespacesFactory.getDocumentNamespaces;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

public class AdditionalNamespacesFactoryTest {

  private static final String NAMESPACES_SAMPLE = "namespaces.xml";
  private static final Path NAMESPACES_PATH = Paths.get("namespaces");
  private static final Path NAMESPACES_SAMPLE_PATH = NAMESPACES_PATH.resolve(NAMESPACES_SAMPLE);

  private List<Namespace> namespaces = new ArrayList<>();
  private Document document;

  @Before
  public void setUp() throws Exception {
    document = getDocument(this.getClass().getClassLoader().getResource(NAMESPACES_SAMPLE_PATH.toString()).toURI().getPath());
  }

  @Test
  public void getAdditionalNamespacesTest() throws Exception {
    namespaces = getAdditionalNamespaces();
    assertThat("The list generated is empty", namespaces.size(), is(greaterThan(0)));
    assertThat("The namespace doesn't match", namespaces.get(0).getPrefix(), is("http"));
  }

  @Test
  public void getDocumentNamespacesTest() throws Exception {
    namespaces = getDocumentNamespaces(document);

    assertThat("The list generate is empty",
               namespaces.stream().filter(n -> n.getPrefix().equals("spring-pepe")).collect(Collectors.toList()).size(),
               is(greaterThan(0)));
  }
}
