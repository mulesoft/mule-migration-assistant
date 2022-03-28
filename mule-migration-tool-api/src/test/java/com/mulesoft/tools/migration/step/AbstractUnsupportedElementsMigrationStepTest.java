/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.step;

import com.google.common.collect.ImmutableList;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.mulesoft.tools.migration.step.AbstractUnsupportedElementsMigrationStep.COMPONENTS_UNSUPPORTED_ERROR_KEY;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Mulesoft Inc.
 */
public class AbstractUnsupportedElementsMigrationStepTest {

  public static final Namespace TEST_NAMESPACE = Namespace.getNamespace("test", "http://test.com");

  private AbstractUnsupportedElementsMigrationStep migrationStep;
  private Element elementMock;
  private Element parentElementMock;
  private MigrationReport reportMock;
  private static final String BLOCKED_ELEMENT_NAME = "blocked";

  @Before
  public void setUp() throws Exception {
    migrationStep = new AbstractUnsupportedElementsMigrationStepTest.MigrationStepImpl();
    elementMock = mock(Element.class);
    parentElementMock = mock(Element.class);
    when(elementMock.getName()).thenReturn(BLOCKED_ELEMENT_NAME);
    when(elementMock.getParentElement()).thenReturn(parentElementMock);
    when(elementMock.getNamespace()).thenReturn(TEST_NAMESPACE);
    reportMock = mock(MigrationReport.class);
    when(reportMock.getComponentKey(elementMock)).thenReturn(BLOCKED_ELEMENT_NAME);
  }

  @Test
  public void testExecute_handleUnsupported() {
    migrationStep.execute(elementMock, reportMock);
    verify(reportMock).report(eq(COMPONENTS_UNSUPPORTED_ERROR_KEY), eq(elementMock), eq(parentElementMock), isA(String.class));
    verify(parentElementMock).removeChild(eq(BLOCKED_ELEMENT_NAME), eq(TEST_NAMESPACE));
  }

  @Test
  public void testExecute_handleAllSupported() {
    when(elementMock.getName()).thenReturn("another_supported_element");
    migrationStep.execute(elementMock, reportMock);
    verifyZeroInteractions(reportMock);
    verifyZeroInteractions(parentElementMock);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecute_NullUnsupportedList() {
    new AbstractUnsupportedElementsMigrationStep(TEST_NAMESPACE) {

      @Override
      public List<String> getUnsupportedElements() {
        return null;
      }
    }.execute(elementMock, reportMock);
  }

  private static final class MigrationStepImpl extends AbstractUnsupportedElementsMigrationStep {

    public MigrationStepImpl() {
      super(TEST_NAMESPACE);
    }

    @Override
    public List<String> getUnsupportedElements() {
      return ImmutableList.of(BLOCKED_ELEMENT_NAME);
    }
  }
}
