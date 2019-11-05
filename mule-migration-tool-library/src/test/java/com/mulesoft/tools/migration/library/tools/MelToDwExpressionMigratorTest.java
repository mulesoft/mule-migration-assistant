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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.project.model.pom.PomModelUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

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
  public void migrateMelSubscript1() {
    String script = "#[message.inboundProperties['http.query.params'].lastname]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[vars.compatibility_inboundProperties['http.query.params'].lastname]"));
  }

  @Test
  public void migrateMelInterpolation1() {
    String script = "Hello \"#[message.inboundProperties['http.query.params'].lastname]\" Max";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[\"Hello '$(vars.compatibility_inboundProperties['http.query.params'].lastname)' Max\"]"));
  }

  @Test
  public void migrateMelInterpolation3() {
    String script = "Hello '#[message.inboundProperties['http.query.params'].lastname]' Max";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[\"Hello '$(vars.compatibility_inboundProperties['http.query.params'].lastname)' Max\"]"));
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
    verify(reportMock).report(eq("expressions.melToDw"), eq(elementMock), eq(elementMock));
    assertThat("Migrated expression is not the expected", migratedExpression, equalTo("#[mel:" + originalExpression + "]"));
  }

  @Test
  public void migrateInstanceOfExpression() {
    PomModel pomModel = PomModelUtils.buildMinimalMule4ApplicationPom("org.fake", "fake-app", "1.0.0", "mule-application");
    when(modelMock.getPomModel()).thenReturn(Optional.of(pomModel));
    Element elementMock = mock(Element.class);
    String originalExpression = "a instanceof org.pepe.Pepito";

    assertThat("Pom model should not have the java module dependency",
               pomModel.getDependencies().stream().noneMatch(d -> d.getArtifactId().equals("mule-java-module")));

    String migratedExpression = expressionMigrator.migrateExpression("#[" + originalExpression + "]", false, elementMock);

    assertThat("Migrated expression is not the expected", migratedExpression,
               equalTo("#[%dw 2.0 --- Java::isInstanceOf(vars.a, 'org.pepe.Pepito')]"));

    assertThat("Pom model should have the java module dependency",
               pomModel.getDependencies().stream().anyMatch(d -> d.getArtifactId().equals("mule-java-module")));
  }

  @Test
  public void migrateEncode64Method() {
    String script = "#[org.apache.commons.codec.binary.Base64.encodeBase64(flowVars['your_variable'].getBytes())] ";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[dw::core::Binaries::toBase64(vars['your_variable'])]"));
  }

  @Test
  public void migrateAndExpression() {
    String script = "#[a && b] ";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[vars.a and vars.b]"));
  }

  @Test
  public void migrateOrExpression() {
    String script = "#[a || b] ";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[vars.a or vars.b]"));
  }

  @Test
  public void migrateTernaryExpression() {
    String script = "#[a || b ? 1 : 0] ";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[if (vars.a or vars.b)   1 else   0]"));
  }

  @Test
  public void migrateTernaryExpression2() {
    String script = "#[a && b && c ? \"max\" : \"mule\"] ";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[if (vars.a and vars.b and vars.c)   'max' else   'mule']"));
  }

  @Test
  public void migrateTernaryExpression3() {
    String script = "#[true ? 1+1 : 4] ";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[if (true)   1 + 1 else   4]"));
  }

  @Test
  public void migrateTernaryExpression4() {
    String script =
        "#[1 + 1 == 2 ? message.inboundProperties['http.relative.path'] : message.inboundProperties['http.query.params'].lastname] ";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result,
               is("#[if (1 + 1 == 2)   vars.compatibility_inboundProperties['http.relative.path'] else   vars.compatibility_inboundProperties['http.query.params'].lastname]"));
  }

  @Test
  public void migrateTernaryExpression5() {
    String script = "#[a instanceof org.pepe.Pepito ? \"is Pepito\" : \"it is not Pepito\"]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[if (Java::isInstanceOf(vars.a, 'org.pepe.Pepito'))   'is Pepito' else   'it is not Pepito']"));
  }

  @Test
  public void migrateTernaryExpression6() {
    String script = "#[timeNow ? server.dateTime : \"\"]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[if (vars.timeNow)   now() else   '']"));
  }

  @Test
  public void migrateLengthMethod() {
    String script = "#[payload.length()]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[length(payload)]"));
  }

  @Test
  public void migrateLengthMethod2() {
    String script = "#[lala.pepe.length()]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[length(lala.pepe)]"));
  }

  @Test
  public void migrateLengthMethod3() {
    String script = "#[lala.pepe.length() + 1]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[length(lala.pepe) + 1]"));
  }

  @Test
  public void migrateLengthMethod4() {
    String script = "#[payload.length() == 0 ? 'empty' : 'not empty']";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[if (length(payload) == 0)   'empty' else   'not empty']"));
  }

  @Test
  public void migrateSizeMethod() {
    String script = "#[payload.size()]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[sizeOf(payload)]"));
  }

  @Test
  public void migrateSizeMethod2() {
    String script = "#[pepe.lala.size()]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[sizeOf(pepe.lala)]"));
  }

  @Test
  public void migrateSizeMethod3() {
    String script = "#[payload.size()*2 + 1]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[sizeOf(payload) * 2 + 1]"));
  }

  @Test
  public void migrateStaticMethod() {
    String script = "#[java.util.Objects.equals(a,b) ? 'equals' : 'not equals']";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[if (java!java::util::Objects::equals(vars.a, vars.b))   'equals' else   'not equals']"));
  }

  @Test
  public void migrateStaticMethod2() {
    String script = "#[java.applet.Applet.newAudioClip(url)]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[java!java::applet::Applet::newAudioClip(vars.url)]"));
  }

  @Test
  public void migrateStaticMethod3() {
    String script = "#[java.beans.EventHandler.create(a,b,c,d)]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[java!java::beans::EventHandler::create(vars.a, vars.b, vars.c, vars.d)]"));
  }

  @Test
  public void migrateStaticMethod4() {
    String script = "#[java.lang.reflect.AccessibleObject.setAccessible(arr,flag)]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[java!java::lang::reflect::AccessibleObject::setAccessible(vars.arr, vars.flag)]"));
  }


  @Test
  public void migrateStaticMethod5() {
    String script = "#[java.lang.reflect.AccessibleObject.setAccessible(arr,flag)]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[java!java::lang::reflect::AccessibleObject::setAccessible(vars.arr, vars.flag)]"));
  }

  /**
   * The migration of static methods works as follows:
   * - The grammar recognizes that the current string is a method invocation
   * - The string has the form "suffix.method(arguments...)" and is parsed into three different parts: suffix, method, and list(arguments)
   * - We have then two options:
   *  1. suffix is something that can be loaded by the current class loader. It means the invocation is a static method and so we migrate it properly to DataWeave
   *  2. suffix isn't something that can be loaded by the current class loader. Then this option is split in another three suboptions:
   *  2.1 method is length() => migrate suffix.length() to lenght(suffix)
   *  2.2 method is size() => migrate suffix.size() to sizeOf(suffix)
   *  2.3 any other case => does not migrate (add NonMigratable metadata with according report key)
   */
  @Test
  public void migrateStaticMethod6() {
    String script = "#[not.in.classloader.Pepe.invoke()]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[mel:not.in.classloader.Pepe.invoke()]"));
  }

  @Test
  public void migrateFunctionExpressionWithInterpolation() {
    String script = "#[function:date].txt";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[\"$(now()).txt\"]"));
  }

  @Test
  public void migrateFunctionExpressionWithInterpolation1() {
    String script = "#[function:datestamp:dd-MM-yyyy].csv";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[\"$(now() as String {format: \"${dd-MM-yyyy}\"}).csv\"]"));
  }

  @Test
  public void migrateEquals() {
    String script = "#[a.equals(b)]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[vars.a == vars.b]"));
  }

  @Test
  public void migrateEquals1() {
    String script = "#[flowVars.ingestResourceType.equals(flowVars.ingestPaths.nodesResource)]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[vars.ingestResourceType == vars.ingestPaths.nodesResource]"));
  }

  @Test
  public void migrateStringWithPlaceholder() {
    String script = "#['Hello ${world}']";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#['Hello ${world}']"));
  }

  @Test
  public void migrateProperties() {
    String script = "#[${world.pepe}]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[p('world.pepe')]"));
  }

  @Test
  public void migrateProperties1() {
    String script = "Hello ${world}";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("Hello ${world}"));
  }

  @Test
  public void migratePayloadAs() {
    String script = "#[message.payloadAs(java.lang.String)]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[write(payload) as String]"));
  }

  @Test
  public void migratePayloadAs1() {
    String script = "#[message.payloadAs(String)]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[write(payload) as String]"));
  }

  @Test
  public void migratePayloadAsConcatenation() {
    String script = "#['payload: ' + message.payloadAs(java.lang.String)]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#['payload: ' ++ write(payload) as String]"));
  }

  @Test
  public void migratePayloadAsInInterpolation() {
    String script = "Payload: #[message.payloadAs(java.lang.String)]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[\"Payload: $(write(payload) as String)\"]"));
  }

  @Test
  public void migratePayloadAsFailure() {
    String script = "#[message.payloadAs(com.lala.Pepe)]";
    Element elementMock = mock(Element.class);
    String result = expressionMigrator.migrateExpression(script, true, elementMock);
    verify(reportMock).report(eq("expressions.melToDw"), eq(elementMock), eq(elementMock));
    verify(reportMock).report(eq("expressions.methodInvocation"), eq(elementMock), eq(elementMock));
    assertThat(result, is("#[mel:message.payloadAs(com.lala.Pepe)]"));
  }

  @Test
  public void migrateSystemCurrentTimeMillis() {
    String script = "#[System.currentTimeMillis()]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[now()]"));
  }

  @Test
  public void migrateCurrentTimeMillisFailure() {
    String script = "#[MyClass.currentTimeMillis()]";
    Element elementMock = mock(Element.class);
    String result = expressionMigrator.migrateExpression(script, true, elementMock);
    verify(reportMock).report(eq("expressions.melToDw"), eq(elementMock), eq(elementMock));
    verify(reportMock).report(eq("expressions.methodInvocation"), eq(elementMock), eq(elementMock));
    assertThat(result, is("#[mel:MyClass.currentTimeMillis()]"));
  }

  @Test
  public void migrateToString() {
    String script = "#[pepe.toString()]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[write(pepe) as String]"));
  }

  @Test
  public void migrateToString1() {
    String script = "#[flowVars.pepe.toString()]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[write(vars.pepe) as String]"));
  }

  @Test
  public void migrateJavaUUI() {
    String script = "#[java.util.UUID.randomUUID()]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[uuid()]"));
  }

  @Test
  public void migrateContains() {
    String script = "#[payload.contains(2)]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[payload contains 2]"));
  }

  @Test
  public void migrateContains1() {
    String script = "#[payload.contains(flowVars.pepe.toString())]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[payload contains write(vars.pepe) as String]"));
  }

  @Test
  public void migrateContains2() {
    String script = "#[flowVars.fileName contains 'GenericExtract']";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[vars.fileName contains 'GenericExtract']"));
  }

  @Test
  public void migrateContains3() {
    String script = "#[payload['pepe'] contains flowVars.pepe.toString()]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[payload['pepe'] contains write(vars.pepe) as String]"));
  }

  @Test
  public void migrateModulus() {
    String script = "#[10 % 4]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[10 mod 4]"));
  }

  @Test
  public void migrateModulus1() {
    String script = "#[flowVars.pepe % payload['lala']]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[vars.pepe mod payload['lala']]"));
  }

  @Test
  public void migrateMessageEncoding() {
    String script = "#[message.dataType.encoding.contains('UTF')]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[message.^encoding contains 'UTF']"));
  }

  @Test
  public void migrateMessageMediaType() {
    String script = "#[message.dataType.mimeType.contains('json')]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[message.^mediaType contains 'json']"));
  }

  @Test
  public void migrateServerIp() {
    String script = "#[server.ip]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[server.ip]"));
  }

  @Test
  public void migrateServerHost() {
    String script = "#[server.host]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[server.host]"));
  }

  @Test
  public void migrateExceptionInstanceOf() {
    String script = "#[exception instanceof org.mule.api.MessagingException]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[Java::isInstanceOf(exception, 'org.mule.api.MessagingException')]"));
  }

  @Test
  public void migrateExceptionCausedExactlyBy() {
    String script = "#[exception.causedExactlyBy(java.util.concurrent.TimeoutException)]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result, is("#[mel:exception.causedExactlyBy(java.util.concurrent.TimeoutException)]"));
  }

  @Test
  public void migrateNestedExceptionExpression() {
    String script =
        "#[exception instanceof org.mule.api.MessagingException && exception.causedExactlyBy(java.util.concurrent.TimeoutException)]";
    String result = expressionMigrator.migrateExpression(script, true, null);
    assertThat(result,
               is("#[Java::isInstanceOf(exception, 'org.mule.api.MessagingException') and mel:exception.causedExactlyBy(java.util.concurrent.TimeoutException)]"));
  }

}
