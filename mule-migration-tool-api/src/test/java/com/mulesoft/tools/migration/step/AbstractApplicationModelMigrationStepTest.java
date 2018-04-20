/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.step;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.step.category.ExpressionMigrator;
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
