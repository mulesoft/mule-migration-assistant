/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.e2e;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
public class HtmlReportTestCase extends AbstractEndToEndTestCase {

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "html_report"
    };
  }

  private final String appToMigrate;

  public HtmlReportTestCase(String appToMigrate) {
    this.appToMigrate = appToMigrate;
  }

  @Test
  @Ignore
  public void test() throws Exception {
    simpleCase(appToMigrate);
  }
}
