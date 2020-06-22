/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.core.component.EchoComponent;
import com.mulesoft.tools.migration.library.mule.steps.core.component.LogComponent;
import com.mulesoft.tools.migration.library.mule.steps.core.component.NullComponent;
import com.mulesoft.tools.migration.tck.ReportVerification;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(Parameterized.class)
public class ComponentTest {

  private static final Path FLOW_EXAMPLES_PATH = Paths.get("mule/apps/core");

  @Rule
  public ReportVerification report = new ReportVerification();

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "component-01",
        "component-01i",
        "component-01ig",
        "component-02",
        "component-02i",
        "component-02ig",
        "component-03",
        "component-03i",
        "component-03ig"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public ComponentTest(String filePrefix) {
    configPath = FLOW_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = FLOW_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private EchoComponent echoComp;
  private LogComponent logComp;
  private NullComponent nullComp;
  private RemovedElements removed;
  private JavaReferenceElements javaRef;

  @Before
  public void setUp() throws Exception {
    echoComp = new EchoComponent();
    logComp = new LogComponent();
    nullComp = new NullComponent();
    removed = new RemovedElements();
    javaRef = new JavaReferenceElements();
  }


  @Test
  public void execute() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());

    getElementsFromDocument(doc, echoComp.getAppliedTo().getExpression())
        .forEach(node -> echoComp.execute(node, report.getReport()));
    getElementsFromDocument(doc, logComp.getAppliedTo().getExpression())
        .forEach(node -> logComp.execute(node, report.getReport()));
    getElementsFromDocument(doc, nullComp.getAppliedTo().getExpression())
        .forEach(node -> nullComp.execute(node, report.getReport()));
    getElementsFromDocument(doc, removed.getAppliedTo().getExpression())
        .forEach(node -> removed.execute(node, report.getReport()));
    getElementsFromDocument(doc, javaRef.getAppliedTo().getExpression())
        .forEach(node -> javaRef.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils
                   .toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                       .ignoreComments().normalizeWhitespace());
  }

}
