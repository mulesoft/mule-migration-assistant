/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

public class DataMapperTest {

  private MigrationReport reportSpy;
  private DataMapper dataMapperStep;
  private Element elementMock;

  @Before
  public void setUp() throws Exception {
    reportSpy = spy(MigrationReport.class);
    dataMapperStep = new DataMapper();
    elementMock = mock(Element.class);
  }

  @Test
  public void execute() {
    dataMapperStep.execute(elementMock, reportSpy);
    verify(reportSpy).report(eq("expressions.datamapper"), eq(elementMock), eq(elementMock));
  }
}
