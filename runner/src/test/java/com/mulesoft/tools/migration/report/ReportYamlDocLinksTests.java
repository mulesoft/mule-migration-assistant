/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.report;

import static java.util.Collections.emptyList;
import static java.util.Collections.list;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(Parameterized.class)
public class ReportYamlDocLinksTests {

  @Parameters(name = "{0}")
  public static Collection<URL> data() throws IOException {
    return list(DefaultMigrationReport.class.getClassLoader().getResources("report.yaml"));
  }

  private URL yamlUrl;

  public ReportYamlDocLinksTests(URL yamlUrl) {
    this.yamlUrl = yamlUrl;
  }

  private Map<String, Map<String, Map<String, Object>>> possibleEntries;

  @Before
  public void loadYaml() throws IOException {
    possibleEntries = new HashMap<>();
    try (InputStream yamlStream = yamlUrl.openStream()) {
      possibleEntries.putAll(new Yaml().loadAs(yamlStream, Map.class));
    }
  }

  @Test
  public void testdocLinks() {
    possibleEntries.values()
        .stream()
        .flatMap(group -> group.values().stream())
        .flatMap(entryData -> ((List<String>) (entryData.get("docLinks") != null ? entryData.get("docLinks") : emptyList()))
            .stream())
        .distinct()
        .forEach(docLink -> {
          pingURL(docLink, 50000);
        });
  }

  /**
   * Pings a HTTP URL. This effectively sends a HEAD request and returns <code>true</code> if the response code is in the 200-399
   * range.
   *
   * @param url The HTTP URL to be pinged.
   * @param timeout The timeout in millis for both the connection timeout and the response read timeout. Note that the total
   *        timeout is effectively two times the given timeout.
   * @return <code>true</code> if the given HTTP URL has returned response code 200-399 on a HEAD request within the given
   *         timeout, otherwise <code>false</code>.
   */
  public static void pingURL(String url, int timeout) {
    try {
      HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
      connection.setConnectTimeout(timeout);
      connection.setReadTimeout(timeout);
      connection.setRequestMethod("HEAD");
      int responseCode = connection.getResponseCode();

      // assertThat(url, responseCode, lessThanOrEqualTo(200));
      assertThat(url, responseCode, lessThanOrEqualTo(399));
    } catch (IOException exception) {
      fail(url + " -> " + exception.getClass().getName() + ": " + exception.getMessage());
    }
  }
}
