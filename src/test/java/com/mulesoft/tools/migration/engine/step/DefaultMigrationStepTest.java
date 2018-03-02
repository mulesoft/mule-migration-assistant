/*
 * Copyright (c) 2015 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.step;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mulesoft Inc.
 */
public class DefaultMigrationStepTest {

  private DefaultMigrationStep migrationStep;
  private ApplicationModel applicationModelMock;

  @Before
  public void setUp() throws Exception {
    migrationStep = new MigrationStepImpl();
    applicationModelMock = mock(ApplicationModel.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void setApplicationModelNull() {
    migrationStep.setApplicationModel(null);
  }

  @Test
  public void setApplicationModel() {
    migrationStep.setApplicationModel(applicationModelMock);
    assertThat("The application model is not as expected", migrationStep.getApplicationModel(), is(applicationModelMock));
  }

  private static final class MigrationStepImpl extends DefaultMigrationStep {

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public void execute() throws Exception {

    }
  }
}
