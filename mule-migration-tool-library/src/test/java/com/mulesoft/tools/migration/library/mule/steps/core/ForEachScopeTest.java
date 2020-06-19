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
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.tck.ReportVerification;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ForEachScopeTest {

  private static final String FILE_SAMPLE_XML = "forEachScope.xml";
  private static final String REMOVE_JSON_TRANSFORMER_NAME = "json-to-object-transformer";
  private static final String REMOVE_BYTE_ARRAY_TRANSFORMER_NAME = "byte-array-to-object-transformer";
  private static final Path FILE_EXAMPLES_PATH = Paths.get("mule/examples/core");
  private static final Path FILE_SAMPLE_PATH = FILE_EXAMPLES_PATH.resolve(FILE_SAMPLE_XML);

  @Rule
  public ReportVerification report = new ReportVerification();

  private ForEachScope forEachScope;
  private Element node;

  @Before
  public void setUp() throws Exception {
    forEachScope = new ForEachScope();
  }

  @Test(expected = MigrationStepException.class)
  public void executeWithNullElement() throws Exception {
    forEachScope.execute(null, report.getReport());
  }

  @Test
  public void executeWithJsonTransFormer() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(FILE_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, forEachScope.getAppliedTo().getExpression()).get(0);
    forEachScope.execute(node, report.getReport());

    Element parent = node.getParentElement();
    assertThat("The node didn't change", parent.getChildren(REMOVE_JSON_TRANSFORMER_NAME), is(empty()));
  }

  @Test
  public void executeWithByteArrayTransFormer() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(FILE_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, forEachScope.getAppliedTo().getExpression()).get(2);
    forEachScope.execute(node, report.getReport());

    Element parent = node.getParentElement();
    assertThat("The node didn't change", parent.getChildren(REMOVE_BYTE_ARRAY_TRANSFORMER_NAME), is(empty()));
  }

  @Test
  public void executeWithNoTransformerToRemoveNotFail() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(FILE_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, forEachScope.getAppliedTo().getExpression()).get(1);
    forEachScope.execute(node, report.getReport());
  }

}
