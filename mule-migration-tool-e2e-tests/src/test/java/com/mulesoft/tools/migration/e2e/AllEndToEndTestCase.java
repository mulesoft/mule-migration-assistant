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

import static java.util.Objects.requireNonNull;


@RunWith(Parameterized.class)
public class AllEndToEndTestCase extends AbstractEndToEndTestCase {

  // e.g. use ".*" to include all tests; "http/.*" for http tests only
  private static final String TEST_INCLUDE = "integration/exception_strategies";

  // e.g. use "" to avoid exclusions; "apikit/.*|domain1app1" OR excludes
  private static final String TEST_EXCLUDE = "";

  @Parameterized.Parameters(name = "{0}-no-compat={1}")
  public static Object[][] params() throws Exception {
    File[] e2eResources = requireNonNull(new File(getResourceUri("e2e")).listFiles());
    List<Object[]> e2eTests = new ArrayList<>();
    collectTests(e2eResources, e2eTests);

    Object[][] parameters = new Object[e2eTests.size()][2];
    int count = 0;
    for (Object[] entry : e2eTests) {
      parameters[count][0] = entry[0];
      parameters[count][1] = entry[1];
      count++;
    }
    return parameters;
  }

  private static void collectTests(File[] dirs, List<Object[]> acu) throws Exception {
    for (File dir : dirs) {
      if (!dir.isDirectory())
        return;
      List<String> subDirs = Arrays.asList(dir.list());
      if (subDirs.contains("input")) {
        String test = dir.getPath().replaceFirst(".*[/\\\\]e2e[/\\\\]", "");
        if (test.matches(TEST_INCLUDE) && !test.matches(TEST_EXCLUDE)) {
          if (subDirs.contains("output"))
            acu.add(new Object[] {test, false});
          if (subDirs.contains("output" + NO_COMPATIBILITY_SUFFIX))
            acu.add(new Object[] {test, true});
        }
      } else {
        collectTests(dir.listFiles(), acu);
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
  private final Boolean noCompatibility;

  public AllEndToEndTestCase(String artifactToMigrate, Boolean noCompatibility) {
    this.artifactName = artifactToMigrate;
    this.noCompatibility = noCompatibility;
  }

  @Test
  public void test() throws Exception {
    String additionalParams = resolveParams(new File(getResourceUri("e2e/" + artifactName)));
    additionalParams += noCompatibility ? " -noCompatibility" : "";
    simpleCase(artifactName, additionalParams.trim().split("\\s"));
  }
}
