/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.xml.AdditionalNamespacesFactory.getDocumentNamespaces;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.IsNot.not;

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
  public void getDocumentNamespacesTest() throws Exception {
    namespaces = getDocumentNamespaces(document, newArrayList());

    assertThat("The list generate is empty",
               namespaces.stream().filter(n -> n.getPrefix().equals("spring-pepe")).collect(Collectors.toList()),
               is(not(empty())));
  }
}
