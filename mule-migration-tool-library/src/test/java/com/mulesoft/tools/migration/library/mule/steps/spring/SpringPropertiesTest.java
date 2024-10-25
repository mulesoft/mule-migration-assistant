/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.spring;

import static java.util.Arrays.asList;

import java.util.Collection;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SpringPropertiesTest extends SpringTest {

  @Parameters(name = "{0}, {1}")
  public static Collection<Object[]> data() {
    return asList(new Object[][] {
        {"spring-13", "4.2.0"}
    });
  }

  public SpringPropertiesTest(String filePrefix, String muleVersion) {
    super(filePrefix, muleVersion);
  }

  @Before
  public void setUp() throws Exception {
    report.expectReportEntry("configProperties.springAttributes");
    super.setUp();
  }

}
