/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.scripting;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

public class ScriptingModuleMigrationTest {

  private static final String SCRIPTING_SAMPLE_XML = "scripting-test-cases.xml";
  private static final Path SCRIPTING_EXAMPLES_PATH = Paths.get("mule/examples/scripting");
  private static final Path SCRIPTING_SAMPLE_PATH = SCRIPTING_EXAMPLES_PATH.resolve(SCRIPTING_SAMPLE_XML);

  private ScriptingModuleMigration scriptingModuleMigration;
  private Element node;

  @Before
  public void setUp() throws Exception {
    scriptingModuleMigration = new ScriptingModuleMigration();
  }

  @Test
  public void executeScriptWithFile() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(SCRIPTING_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, scriptingModuleMigration.getAppliedTo().getExpression()).get(1);
    scriptingModuleMigration.execute(node, mock(MigrationReport.class));

    assertThat("The name didn't change.", node.getName(), equalTo("execute"));
    assertThat("The file attribute wasn't moved.", node.getChildren().get(0).getValue(), equalTo("${file::pepe.java}"));
  }

  @Test
  public void executeScriptWithCode() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(SCRIPTING_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, scriptingModuleMigration.getAppliedTo().getExpression()).get(2);
    scriptingModuleMigration.execute(node, mock(MigrationReport.class));

    assertThat("The name didn't change.", node.getName(), equalTo("execute"));
    assertThat("The file attribute wasn't moved.", node.getAttribute("engine").getValue(), equalTo("nashorn"));
  }

  @Test
  public void executeScriptWithParams() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(SCRIPTING_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, scriptingModuleMigration.getAppliedTo().getExpression()).get(0);
    scriptingModuleMigration.execute(node, mock(MigrationReport.class));

    assertThat("The name didn't change.", node.getName(), equalTo("execute"));
    assertThat("The attributes weren't moved..", node.getChildren().get(1).getName(), equalTo("parameters"));
  }

  @Test
  public void executeScriptWithGroovyCode() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(SCRIPTING_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, scriptingModuleMigration.getAppliedTo().getExpression()).get(3);
    scriptingModuleMigration.execute(node, mock(MigrationReport.class));

    assertThat("The name didn't change.", node.getName(), equalTo("execute"));
    assertThat("The file attribute wasn't moved.", node.getAttribute("engine").getValue(), equalTo("groovy"));
  }

  @Test
  public void executeScriptWithRuby() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(SCRIPTING_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, scriptingModuleMigration.getAppliedTo().getExpression()).get(4);
    scriptingModuleMigration.execute(node, mock(MigrationReport.class));

    assertThat("The name didn't change.", node.getName(), equalTo("execute"));
    assertThat("The file attribute wasn't moved.", node.getAttribute("engine").getValue(), equalTo("ruby"));
  }

  @Test
  public void executeScriptWithPython() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(SCRIPTING_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, scriptingModuleMigration.getAppliedTo().getExpression()).get(5);
    scriptingModuleMigration.execute(node, mock(MigrationReport.class));

    assertThat("The name didn't change.", node.getName(), equalTo("execute"));
    assertThat("The file attribute wasn't moved.", node.getAttribute("engine").getValue(), equalTo("jython"));
  }
}
