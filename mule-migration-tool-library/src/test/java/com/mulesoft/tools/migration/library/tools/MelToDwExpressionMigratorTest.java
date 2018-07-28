/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.tools;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class MelToDwExpressionMigratorTest {

  private ExpressionMigrator expressionMigrator;
  private MigrationReport reportMock;
  private ApplicationModel modelMock;

  @Before
  public void setUp() {
    reportMock = mock(MigrationReport.class);
    modelMock = mock(ApplicationModel.class);
    expressionMigrator = new MelToDwExpressionMigrator(reportMock, modelMock);
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

  @Test(expected = NullPointerException.class)
  public void unwrapNull() {
    expressionMigrator.unwrap(null);
  }

  @Test(expected = NullPointerException.class)
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
    assertThat(result, is("#[\"$(vars.compatibility_inboundProperties.originalFilename)_$(correlationId)\"]"));
  }

  @Test
  public void migrateMelInterpolationWithMelPrefix() {
    String script = "#[mel:message.inboundProperties.originalFilename]_#[message.id]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[\"$(vars.compatibility_inboundProperties.originalFilename)_$(correlationId)\"]"));
  }

  @Test
  public void migrateMelInterpolation2() {
    String script = "#[message.outboundProperties.name]_#[message.id]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[\"$(vars.compatibility_outboundProperties.name)_$(correlationId)\"]"));
  }

  @Test
  public void migrateMelInterpolationWithMelPrefix2() {
    String script = "#[mel:message.outboundProperties.name]_#[message.id]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[\"$(vars.compatibility_outboundProperties.name)_$(correlationId)\"]"));
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

  @Test
  public void migrateStringConcatenationTwoLiterals() {
    String script = "#['java' + 'script']";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#['java' ++ 'script']"));
  }

  @Test
  public void migrateStringConcatenationLeftVariableIsCoerced() {
    String script = "#[foo + 'bar']";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[vars.foo ++ 'bar']"));
  }

  @Test
  public void migrateStringConcatenationRightVariableIsCoerced() {
    String script = "#['foo' + bar]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#['foo' ++ vars.bar]"));
  }

  @Test
  public void migrateStringConcatenationLeftIntegerIsCoerced() {
    String script = "#[14 + 'la']";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[14 ++ 'la']"));
  }

  @Test
  public void migrateStringConcatenationRightIntegerIsCoerced() {
    String script = "#['la' + 14]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#['la' ++ 14]"));
  }

  @Test
  public void migrateStringConcatenationMoreThanTwoElementsAtLeastOneString() {
    String script = "#[14 + 'la' + 14]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[14 ++ 'la' ++ 14]"));
  }

  @Test
  public void migrateStringConcatenationWithIdentifier() {
    String script = "#[payload + 'reload']";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[payload ++ 'reload']"));
  }

  @Test
  public void migrateStringConcatenationWithMoreThanOneIdentifier() {
    String script = "#[payload.foo + payload.bar + 'max']";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[payload.foo ++ payload.bar ++ 'max']"));
  }

  @Test
  public void migrateStringConcatenationWithMoreThanOneIdentifier2() {
    String script = "#['max' + payload.foo + payload.bar]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#['max' ++ payload.foo ++ payload.bar]"));
  }

  @Test
  public void migrateSum() {
    String script = "#[1 + 1]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[1 + 1]"));
  }

  @Test
  public void migrateSumWithIdentifier() {
    String script = "#[payload + 1]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[payload + 1]"));
  }

  @Test
  public void migrateComplexConcatenation() {
    String script =
        "#['Successfully redirected: ' + message.inboundProperties['http.relative.path'] + '?' + message.inboundProperties['http.query.string']]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result,
               is("#['Successfully redirected: ' ++ vars.compatibility_inboundProperties['http.relative.path'] ++ '?' ++ vars.compatibility_inboundProperties['http.query.string']]"));
  }

  @Test
  public void migrateComplexConcatenation2() {
    String script =
        "#['Redirecting to: ' + message.outboundProperties['path'] + '?' + message.outboundProperties.path2]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result,
               is("#['Redirecting to: ' ++ vars.compatibility_outboundProperties['path'] ++ '?' ++ vars.compatibility_outboundProperties.path2]"));
  }

  @Test
  public void migrateServerDateTime() {
    String script = "#[server.dateTime]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[now()]"));
  }

  @Test
  public void migrateServerNanoSeconds() {
    String script = "#[server.nanoSeconds]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[System.nanoTime()]"));
  }


  @Test(expected = NullPointerException.class)
  public void isWrappedNull() {
    expressionMigrator.isWrapped(null);
  }

  @Test
  public void migrateNotMigratableExpression() {
    Element elementMock = mock(Element.class);
    String originalExpression = "OUTBOUND:BLA";
    String migratedExpression = expressionMigrator.migrateExpression("#[" + originalExpression + "]", false, elementMock);
    verify(reportMock).report(eq(MigrationReport.Level.WARN), eq(elementMock), eq(elementMock), anyString(), anyString(),
                              anyString());
    assertThat("Migrated expression is not the expected", migratedExpression, equalTo("#[mel:" + originalExpression + "]"));
  }
}
