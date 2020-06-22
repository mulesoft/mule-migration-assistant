/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.junit.Before;
import org.junit.Test;
import org.jdom2.Element;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;

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
