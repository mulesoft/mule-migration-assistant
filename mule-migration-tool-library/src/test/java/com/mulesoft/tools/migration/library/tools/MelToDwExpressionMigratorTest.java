/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.tools;

import com.mulesoft.tools.migration.step.category.ExpressionMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MelToDwExpressionMigratorTest {

  private ExpressionMigrator expressionMigrator;
  private MigrationReport reportMock;

  @Before
  public void setUp() {
    reportMock = mock(MigrationReport.class);
    expressionMigrator = new MelToDwExpressionMigrator(reportMock);
  }

  @Test
  public void unwrapExpression() {
    String expression = "payload.foo";
    String unwrappedExpression = expressionMigrator.unwrap("#[ " + expression + " ]");
    assertThat("Not the expected unwrapped string", unwrappedExpression, equalTo(expression));
  }

  @Test
  public void unwrapEmpty() {
    String unwrappedExpression = expressionMigrator.unwrap("#[ ]");
    assertThat("Not the expected unwrapped string", unwrappedExpression, equalTo(StringUtils.EMPTY));
  }

  @Test
  public void unwrapList() {
    String listExpression = "[1,2,3]";
    String unwrappedExpression = expressionMigrator.unwrap("#[" + listExpression + "]");
    assertThat("Not the expected unwrapped string", unwrappedExpression, equalTo(listExpression));
  }

  @Test
  public void unwrapEmptyList() {
    String emptyListExpression = "[]";
    String unwrappedExpression = expressionMigrator.unwrap("#[" + emptyListExpression + "]");
    assertThat("Not the expected unwrapped string", unwrappedExpression, equalTo(emptyListExpression));
  }

  @Test
  public void unwrapTwice() {
    String weirdExpression = "#[]";
    String unwrappedExpression = expressionMigrator.unwrap("#[" + weirdExpression + "]");
    assertThat("Not the expected unwrapped string", unwrappedExpression, equalTo(StringUtils.EMPTY));
  }

  @Test
  public void unwrapUnwrappedExpression() {
    String unwrappedListExpression = "[1,2,3]";
    String unwrappedExpression = expressionMigrator.unwrap(unwrappedListExpression);
    assertThat("Not the expected unwrapped string", unwrappedExpression, equalTo(unwrappedListExpression));
  }

  @Test
  public void unwrapUnwrappedEmptyExpression() {
    String unwrappedEmptyExpression = "";
    String unwrappedExpression = expressionMigrator.unwrap(unwrappedEmptyExpression);
    assertThat("Not the expected unwrapped string", unwrappedExpression, equalTo(unwrappedEmptyExpression));
  }

  @Test(expected = IllegalArgumentException.class)
  public void unwrapNull() {
    expressionMigrator.unwrap(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void wrapNull() {
    expressionMigrator.wrap(null);
  }

  @Test
  public void wrapExpression() {
    String expression = "payload.foo";
    String wrappedExpression = expressionMigrator.wrap(expression);
    assertThat("Wrapped expressin is not the expected", wrappedExpression, equalTo("#[" + expression + "]"));
  }

  @Test
  public void isWrappedTrue() {
    List<String> wrappedExpressions = newArrayList("#[]", "#[ ]", "#[payload]", "#[ payload ]", "#[[]]", "#[['#']]");
    for (String expression : wrappedExpressions) {
      assertThat("Method should have returned true", expressionMigrator.isWrapped(expression), is(true));
    }
  }

  @Test
  public void isWrappedFalse() {
    List<String> unwrappedExpressions = newArrayList("", " ", "[]", "[ ]", "payload", "# payload ", "[[]]", "[['#']]");
    for (String expression : unwrappedExpressions) {
      assertThat("Method should have returned false", expressionMigrator.isWrapped(expression), is(false));
    }
  }

  @Test
  public void migrateMelInterpolation() {
    String script = "#[message.inboundProperties.originalFilename]_#[message.id]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[\"$(vars.compatibility_inboundProperties.originalFilename)_$(message.id)\"]"));
  }

  @Test
  public void migrateMelInterpolationWithMelPrefix() {
    String script = "#[mel:message.inboundProperties.originalFilename]_#[message.id]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[\"$(vars.compatibility_inboundProperties.originalFilename)_$(message.id)\"]"));
  }

  @Test
  public void migrateMelEmptyList() {
    String script = "#[[]]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[[]]"));
  }

  @Test
  public void migrateLiteral() {
    String script = "static_value";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is(script));
  }

  @Test(expected = IllegalArgumentException.class)
  public void isWrappedNull() {
    expressionMigrator.isWrapped(null);
  }

  @Test
  public void migrateNotMigratableExpression() {
    Element elementMock = mock(Element.class);
    String originalExpression = "OUTBOUND::BLA";
    String migratedExpression = expressionMigrator.migrateExpression("#[" + originalExpression + "]", false, elementMock);
    verify(reportMock).report(eq(MigrationReport.Level.WARN), eq(elementMock), eq(elementMock), anyString(), anyString(),
                              anyString());
    assertThat("Migrated expression is not the expected", migratedExpression, equalTo("#[mel:" + originalExpression + "]"));
  }
}
