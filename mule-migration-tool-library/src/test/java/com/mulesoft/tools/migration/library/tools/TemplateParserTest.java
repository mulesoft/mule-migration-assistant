/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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
    assertThat("Translated template is not the expected", result, equalTo("#[\"Hello $(greeting) 'World'\"]"));
  }

  @Test
  public void translateQuotedInterpolatedString() {
    String template = "INSERT INTO PLANET(POSITION, NAME) VALUES (777, '#[payload]')";
    String result = parser.translate(template, IDENTITY_TRANSLATOR);
    assertThat("Translated template is not the expected", result,
               equalTo("#[\"INSERT INTO PLANET(POSITION, NAME) VALUES (777, '$(payload)')\"]"));
  }
}
