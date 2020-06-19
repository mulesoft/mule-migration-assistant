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
