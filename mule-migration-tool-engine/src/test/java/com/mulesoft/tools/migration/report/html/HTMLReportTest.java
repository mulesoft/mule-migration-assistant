/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.report.html;

import com.mulesoft.tools.migration.step.category.MigrationReport;
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
    printer = new HTMLReport(mock(MigrationReport.class), temporaryFolder.getRoot(), "1.0.0");
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
