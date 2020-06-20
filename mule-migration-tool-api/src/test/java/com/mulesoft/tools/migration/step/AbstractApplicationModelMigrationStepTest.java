/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
