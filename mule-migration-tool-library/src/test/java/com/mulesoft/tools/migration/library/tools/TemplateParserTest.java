/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.tools;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class TemplateParserTest {

  private TemplateParser parser;
  private static final TemplateParser.ScriptTranslator IDENTITY_TRANSLATOR = (x) -> x;

  @Before
  public void setUp() {
    parser = TemplateParser.createMuleStyleParser();
  }

  @Test
  public void translateContainingSimpleQuotes() {
    String template = "Hello #[greeting] 'World'";
    String result = parser.translate(template, IDENTITY_TRANSLATOR);
    assertThat("Translated template is not the expected", result, equalTo("#['Hello $(greeting) \\\'World\\\'']"));
  }
}
