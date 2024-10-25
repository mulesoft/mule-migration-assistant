/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.report.html;

import org.junit.Test;

import static org.junit.Assert.*;

public class ReportFileWriterTest {

  @Test
  public void getHtmlFileName() throws Exception {
    String name = "example.xml";
    ReportFileWriter writer = new ReportFileWriter();
    assertEquals(writer.getHtmlFileName(name, 0), "example-0.html");
  }

}
