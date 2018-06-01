/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.report.html;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class HTMLReportTest {

  private HTMLReport printer;
  private ReportFileWriter writer;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void setUp() {
    printer = new HTMLReport(mock(List.class), temporaryFolder.getRoot());
    writer = mock(ReportFileWriter.class);
    printer.setReportFileWriter(writer);
  }

  @Test
  public void printingReportWritesFiles() throws IOException {
    printer.printReport();
    verify(writer, times(1)).writeToFile(any(File.class), anyString());
    verify(writer, times(4)).copyFile(anyString(), any(File.class));
  }

}
