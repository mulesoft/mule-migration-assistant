/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.applicationgraph.*;
import com.mulesoft.tools.migration.project.model.applicationgraph.Flow;
import com.mulesoft.tools.migration.tck.ReportVerification;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SetPropertyTest {

  private static final String FILE_SAMPLE_XML = "setProperty.xml";
  private static final Path FILE_EXAMPLES_PATH = Paths.get("mule/examples/core");
  private static final Path FILE_SAMPLE_PATH = FILE_EXAMPLES_PATH.resolve(FILE_SAMPLE_XML);

  @Rule
  public ReportVerification report = new ReportVerification();

  private SetProperty setProperty;
  private Element node;
  private ApplicationModel mockApplicationModel;

  @Before
  public void setUp() throws Exception {
    setProperty = new SetProperty();
    setProperty.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), mock(ApplicationModel.class)));
    mockApplicationModel = mock(ApplicationModel.class);

    setProperty.setApplicationModel(mockApplicationModel);
  }

  @Ignore
  @Test(expected = MigrationStepException.class)
  public void executeWithNullElement() throws Exception {
    setProperty.execute(null, report.getReport());
  }

  @Test
  public void execute() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(FILE_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, setProperty.getAppliedTo().getExpression()).get(0);
    setProperty.execute(node, report.getReport());

    assertThat("The node namespace didn't change", node.getNamespaceURI(),
               is("http://www.mulesoft.org/schema/mule/compatibility"));
    assertThat("The node namespace prefix didn't change", node.getNamespacePrefix(), is("compatibility"));
    assertThat("The node name changed", node.getName(), is("set-property"));
    assertThat("The attribute was renamed", node.getAttribute("propertyName"), is(notNullValue()));
  }

  @Test
  public void execute_noCompatibility() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(FILE_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, setProperty.getAppliedTo().getExpression()).get(0);
    ApplicationGraph mockApplicationGraph = mock(ApplicationGraph.class);
    when(mockApplicationModel.getApplicationGraph()).thenReturn(mockApplicationGraph);

    SetPropertyProcessor flowComponent = new SetPropertyProcessor(node, mock(Flow.class), mockApplicationGraph);
    flowComponent.setPropertiesMigrationContext(new PropertiesMigrationContext(Maps.newHashMap(), ImmutableMap
        .of("propertyName", new PropertyMigrationContext("vars.outbound_propertyName")), null));
    when(mockApplicationGraph.findFlowComponent(isA(Element.class))).thenReturn(flowComponent);

    setProperty.execute(node, report.getReport());

    assertThat("The node namespace changed", node.getNamespaceURI(),
               is("http://www.mulesoft.org/schema/mule/core"));
    assertThat("The node namespace changed", node.getNamespacePrefix(), is(""));
    assertThat("The node name didn't changed", node.getName(), is("set-variable"));
    assertThat("The attribute was not renamed", node.getAttribute("variableName"), is(notNullValue()));
    assertThat("The attribute was not translated", node.getAttribute("variableName").getValue(), is("outbound_propertyName"));
  }
}
