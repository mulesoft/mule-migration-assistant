/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.e2e;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


@RunWith(Parameterized.class)
public class AllEndToEndTestCase extends AbstractEndToEndTestCase {

  // e.g. use ".*" to include all tests; "http/.*" for http tests only
  private static final String TEST_INCLUDE = ".*";

  // e.g. use "" to avoid exclusions; "apikit/.*|domain1app1" OR excludes
  private static final String TEST_EXCLUDE = "";

  @Parameterized.Parameters(name = "{0}")
  public static Object[][] params() throws Exception {
    File[] top = new File(getResourceUri("e2e")).listFiles();
    Map<String, String> tests = new TreeMap<>();
    gatherTests(top, tests);

    Object[][] parameters = new String[tests.size()][2];
    int count = 0;
    for (Map.Entry<String, String> entry : tests.entrySet()) {
      parameters[count][0] = entry.getKey();
      parameters[count][1] = entry.getValue();
      count++;
    }
    return parameters;
  }

  private static void gatherTests(File[] dirs, Map<String, String> acu) throws Exception {
    for (File dir : dirs) {
      if (!dir.isDirectory())
        return;
      if (Arrays.asList(dir.list()).contains("input")) {
        String test = dir.getPath().replaceFirst(".*/e2e/", "");
        if (test.matches(TEST_INCLUDE) && !test.matches(TEST_EXCLUDE)) {
          acu.put(test, resolveParams(dir));
        }
      } else {
        gatherTests(dir.listFiles(), acu);
      }
    }
  }

  private static String resolveParams(File dir) throws Exception {
    File params = new File(dir, "params.txt");
    List<String> result = new ArrayList<>();
    if (params.exists()) {
      List<String> lines = FileUtils.readLines(params, "UTF-8");
      for (String line : lines) {
        if (line.startsWith("-parentDomainBasePath")) {
          String[] split = line.split("\\s");
          result.add(split[0]);
          result.add(new File(getResourceUri(split[1])).getAbsolutePath());
        }
      }
    }
    return String.join(" ", result);
  }

  private final String artifactName;
  private final String additionalParams;

  public AllEndToEndTestCase(String artifactToMigrate, String additionalParams) {
    this.artifactName = artifactToMigrate;
    this.additionalParams = additionalParams;
  }

  @Test
  public void test() throws Exception {
    simpleCase(artifactName, additionalParams.split("\\s"));
  }

}
