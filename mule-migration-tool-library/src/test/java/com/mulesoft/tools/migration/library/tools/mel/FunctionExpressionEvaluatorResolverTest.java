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
package com.mulesoft.tools.migration.library.tools.mel;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;

public class FunctionExpressionEvaluatorResolverTest {

  private FunctionExpressionEvaluatorResolver resolver;
  private Element element;
  private MigrationReport report;
  private ApplicationModel model;
  private ExpressionMigrator migrator;

  @Before
  public void setUp() {
    resolver = new FunctionExpressionEvaluatorResolver();
    element = mock(Element.class);
    report = mock(MigrationReport.class);
    model = mock(ApplicationModel.class);
    migrator = mock(ExpressionMigrator.class);
  }

  @Test
  public void canResolveTrue() {
    assertThat("Resolver should be able to resolve this expression", resolver.canResolve("function:pepe"));
  }

  @Test
  public void canResolveFalse() {
    assertThat("Resolver should not be able to resolve this expression", !resolver.canResolve("functions:pepe"));
  }

  @Test
  public void canResolveFalse1() {
    assertThat("Resolver should not be able to resolve this expression", !resolver.canResolve("lolo:pepe"));
  }

  @Test
  public void canResolveFalse2() {
    assertThat("Resolver should not be able to resolve this expression", !resolver.canResolve("fun:pepe"));
  }

  @Test
  public void canResolveFalse3() {
    assertThat("Resolver should not be able to resolve a null expression", !resolver.canResolve(null));
  }

  @Test
  public void getFunctionName() {
    String functionName = "pepe";
    assertThat("Resolver should not be able to resolve a function name", resolver.getFunctionName("function:" + functionName),
               equalTo(functionName));
  }

  @Test
  public void resolveNow() {
    assertThat("Resolver should not be able to resolve the now function",
               resolver.resolve("function:now", element, report, model, migrator), equalTo("now()"));
  }

  @Test
  public void resolveDate() {
    assertThat("Resolver should be able to resolve the date function",
               resolver.resolve("function:date", element, report, model, migrator), equalTo("now()"));
  }

  @Test
  public void resolveDatestamp() {
    assertThat("Resolver should be able to resolve a default datestamp expression",
               resolver.resolve("function:datestamp", element, report, model, migrator),
               equalTo("now() as String {format: \"dd-MM-yy_HH-mm-ss.SSS\"}"));
  }

  @Test
  public void resolveFormattedDatestamp() {
    assertThat("Resolver should be able to resolve a formatted datestamp expression",
               resolver.resolve("function:datestamp:dd-MM-yy", element, report, model, migrator),
               equalTo("now() as String {format: \"${dd-MM-yy}\"}"));
  }

  @Test
  public void resolveUUID() {
    assertThat("Resolver should be able to resolve the uuid function",
               resolver.resolve("function:uuid", element, report, model, migrator), equalTo("uuid()"));
  }

  @Test
  public void resolveSystime() {
    assertThat("Resolver should be able to resolve the systime function",
               resolver.resolve("function:systime", element, report, model, migrator),
               equalTo("now() as Number {unit: \"milliseconds\"}"));
  }

  @Test
  public void resolveHostname() {
    assertThat("Resolver should be able to resolve the hostname function",
               resolver.resolve("function:hostname", element, report, model, migrator), equalTo("server.host"));
  }

  @Test
  public void resolveIP() {
    assertThat("Resolver should be able to resolve the ip function",
               resolver.resolve("function:ip", element, report, model, migrator), equalTo("server.ip"));
  }

  @Test
  public void resolvePayloadClass() {
    assertThat("Resolver should be able to resolve the payloadClass function",
               resolver.resolve("function:payloadClass", element, report, model, migrator), equalTo("payload.^class"));
  }

  @Test
  public void resolveShortPayloadClass() {
    assertThat("Resolver should be able to resolve the shortPayloadClass function",
               resolver.resolve("function:shortPayloadClass", element, report, model, migrator),
               equalTo("( payload.^class splitBy  '.' )[-1]"));
  }
}
