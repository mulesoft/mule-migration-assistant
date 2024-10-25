/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.step;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.xpath.XPathExpression;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Mulesoft Inc.
 */
public class AbstractApplicationModelMigrationStepTest {

  private AbstractApplicationModelMigrationStep migrationStep;
  private Element elementMock;
  private static final String APPLIED_TO_INVALID = "test-string";

  @Before
  public void setUp() throws Exception {
    migrationStep = new MigrationStepImpl();
    elementMock = mock(Element.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void setAppliedToNull() {
    migrationStep.setAppliedTo(null);
  }

  @Test
  public void setAppliedToInvalid() {
    migrationStep.setAppliedTo(APPLIED_TO_INVALID);
    assertThat("The applied to is not as expected", migrationStep.getAppliedTo(), instanceOf(XPathExpression.class));
  }

  private static final class MigrationStepImpl extends AbstractApplicationModelMigrationStep {

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public void execute(Element object, MigrationReport report) throws RuntimeException {

    }

  }
}
