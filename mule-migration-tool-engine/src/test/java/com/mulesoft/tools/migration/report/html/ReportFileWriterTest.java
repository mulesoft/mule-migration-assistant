/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.report.html;

import org.junit.Test;

import static org.junit.Assert.*;

public class ReportFileWriterTest {

  @Test
  public void getHtmlFileName() throws Exception {
    String name = "example.xml";
    ReportFileWriter writer = new ReportFileWriter();
    assertEquals(writer.getHtmlFileName(name), "example-report.html");
  }

}
