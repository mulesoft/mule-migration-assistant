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
import static org.hamcrest.Matchers.nullValue;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.tck.ReportVerification;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class RemoveObjectToStringTransformerTest {

  private static final String FILE_SAMPLE_XML = "removeObjectToStringTransformer.xml";
  private static final Path FILE_EXAMPLES_PATH = Paths.get("mule/examples/core");
  private static final Path FILE_SAMPLE_PATH = FILE_EXAMPLES_PATH.resolve(FILE_SAMPLE_XML);

  @Rule
  public ReportVerification report = new ReportVerification();

  private RemoveObjectToStringTransformer removeObjectToStringTransformer;
  private Element node;

  @Before
  public void setUp() throws Exception {
    removeObjectToStringTransformer = new RemoveObjectToStringTransformer();
  }

  @Test(expected = MigrationStepException.class)
  public void executeWithNullElement() throws Exception {
    removeObjectToStringTransformer.execute(null, report.getReport());
  }

  @Test
  public void execute() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(FILE_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, removeObjectToStringTransformer.getAppliedTo().getExpression()).get(0);
    removeObjectToStringTransformer.execute(node, report.getReport());

    assertThat("The node wasn't remove.", node.getParent(), is(nullValue()));
  }

}
