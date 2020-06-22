/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.mulesoft.tools.migration.tck.ReportVerification;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(Parameterized.class)
public class TransactionalScopeTest {

  private static final Path FILE_EXAMPLES_PATH = Paths.get("mule/examples/core");

  @Rule
  public ReportVerification report = new ReportVerification();

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "transactionalScope.xml",
        "xaTransactionalScope.xml",
        "multiTransactionalScope.xml"
    };
  }

  private final Path configPath;

  public TransactionalScopeTest(String fileSampleXml) {
    configPath = FILE_EXAMPLES_PATH.resolve(fileSampleXml);
  }

  private TransactionalScope transactionalScope;
  private Element node;

  @Before
  public void setUp() throws Exception {
    transactionalScope = new TransactionalScope();
  }

  @Test
  public void executeWithNullElement() throws Exception {
    transactionalScope.execute(null, report.getReport());
  }

  @Test
  public void execute() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, transactionalScope.getAppliedTo().getExpression()).get(0);
    transactionalScope.execute(node, report.getReport());

    assertThat("The node didn't change", node.getName(), is("try"));
  }
}
