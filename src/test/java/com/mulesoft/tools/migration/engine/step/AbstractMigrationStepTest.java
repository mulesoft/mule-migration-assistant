/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.step;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import com.mulesoft.tools.migration.project.model.ApplicationModel;

import java.util.List;

/**
 * @author Mulesoft Inc.
 */
public class AbstractMigrationStepTest {

  private AbstractMigrationStep migrationStep;
  private Element elementMock;
  private static final String APPLIED_TO = "test-string";

  @Before
  public void setUp() throws Exception {
    migrationStep = new MigrationStepImpl();
    elementMock = mock(Element.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void setApplicationModelNull() {
    migrationStep.setElement(null);
  }

  @Test
  public void setApplicationModel() {
    migrationStep.setElement(elementMock);
    assertThat("The application model is not as expected", migrationStep.getElement(), is(elementMock));
  }

  @Test(expected = IllegalArgumentException.class)
  public void setAppliedToNull() {
    migrationStep.setAppliedTo(null);
  }

  @Test
  public void setAppliedTo() {
    migrationStep.setAppliedTo(APPLIED_TO);
    assertThat("The application model is not as expected", migrationStep.getAppliedTo(), is(APPLIED_TO));
  }

  private static final class MigrationStepImpl extends AbstractMigrationStep {

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public void execute() throws Exception {

    }
  }
}
