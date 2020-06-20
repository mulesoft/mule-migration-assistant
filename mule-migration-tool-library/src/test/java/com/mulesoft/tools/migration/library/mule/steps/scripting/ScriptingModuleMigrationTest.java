/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.mulesoft.tools.migration.library.mule.steps.scripting;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.mulesoft.tools.migration.tck.ReportVerification;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ScriptingModuleMigrationTest {

  private static final String SCRIPTING_SAMPLE_XML = "scripting-test-cases.xml";
  private static final Path SCRIPTING_EXAMPLES_PATH = Paths.get("mule/examples/scripting");
  private static final Path SCRIPTING_SAMPLE_PATH = SCRIPTING_EXAMPLES_PATH.resolve(SCRIPTING_SAMPLE_XML);

  @Rule
  public ReportVerification report = new ReportVerification();

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
    scriptingModuleMigration.execute(node, report.getReport());

    assertThat("The name didn't change.", node.getName(), equalTo("execute"));
    assertThat("The file attribute wasn't moved.", node.getChildren().get(0).getValue(), equalTo("${file::pepe.java}"));
  }

  @Test
  public void executeScriptWithCode() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(SCRIPTING_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, scriptingModuleMigration.getAppliedTo().getExpression()).get(2);
    scriptingModuleMigration.execute(node, report.getReport());

    assertThat("The name didn't change.", node.getName(), equalTo("execute"));
    assertThat("The file attribute wasn't moved.", node.getAttribute("engine").getValue(), equalTo("nashorn"));
  }

  @Test
  public void executeScriptWithParams() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(SCRIPTING_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, scriptingModuleMigration.getAppliedTo().getExpression()).get(0);
    scriptingModuleMigration.execute(node, report.getReport());

    assertThat("The name didn't change.", node.getName(), equalTo("execute"));
    assertThat("The attributes weren't moved..", node.getChildren().get(1).getName(), equalTo("parameters"));
  }

  @Test
  public void executeScriptWithGroovyCode() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(SCRIPTING_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, scriptingModuleMigration.getAppliedTo().getExpression()).get(3);
    scriptingModuleMigration.execute(node, report.getReport());

    assertThat("The name didn't change.", node.getName(), equalTo("execute"));
    assertThat("The file attribute wasn't moved.", node.getAttribute("engine").getValue(), equalTo("groovy"));
  }

  @Test
  public void executeScriptWithRuby() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(SCRIPTING_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, scriptingModuleMigration.getAppliedTo().getExpression()).get(4);
    scriptingModuleMigration.execute(node, report.getReport());

    assertThat("The name didn't change.", node.getName(), equalTo("execute"));
    assertThat("The file attribute wasn't moved.", node.getAttribute("engine").getValue(), equalTo("ruby"));
  }

  @Test
  public void executeScriptWithPython() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(SCRIPTING_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, scriptingModuleMigration.getAppliedTo().getExpression()).get(5);
    scriptingModuleMigration.execute(node, report.getReport());

    assertThat("The name didn't change.", node.getName(), equalTo("execute"));
    assertThat("The file attribute wasn't moved.", node.getAttribute("engine").getValue(), equalTo("jython"));
  }
}
