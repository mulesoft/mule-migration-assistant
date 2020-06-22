/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.step.util;

import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.util.TransportsUtils.EndpointAddress;

import org.jdom2.Element;
import org.junit.Test;

/**
 * @author Mulesoft Inc.
 */
public class TransportUtilsAddressTest {

  @Test
  public void fileRelativePath() {
    EndpointAddress address = doProcessAddress("file://myPath");
    assertThat(address.getProtocol(), is("file"));
    assertThat(address.getCredentials(), is(nullValue()));
    assertThat(address.getHost(), is(nullValue()));
    assertThat(address.getPort(), is(nullValue()));
    assertThat(address.getPath(), is("myPath"));
  }

  @Test
  public void fileAbsolutePath() {
    EndpointAddress address = doProcessAddress("file:///myPath");
    assertThat(address.getProtocol(), is("file"));
    assertThat(address.getCredentials(), is(nullValue()));
    assertThat(address.getHost(), is(nullValue()));
    assertThat(address.getPort(), is(nullValue()));
    assertThat(address.getPath(), is("/myPath"));
  }

  @Test
  public void filePlaceholderPath() {
    EndpointAddress address = doProcessAddress("file://${myPath}");
    assertThat(address.getProtocol(), is("file"));
    assertThat(address.getCredentials(), is(nullValue()));
    assertThat(address.getHost(), is(nullValue()));
    assertThat(address.getPort(), is(nullValue()));
    assertThat(address.getPath(), is("${myPath}"));
  }

  @Test
  public void filePlaceholderPartPath() {
    EndpointAddress address = doProcessAddress("file:///thisIs/${myPath}/really");
    assertThat(address.getProtocol(), is("file"));
    assertThat(address.getCredentials(), is(nullValue()));
    assertThat(address.getHost(), is(nullValue()));
    assertThat(address.getPort(), is(nullValue()));
    assertThat(address.getPath(), is("/thisIs/${myPath}/really"));
  }

  @Test
  public void fileExpressionPath() {
    EndpointAddress address = doProcessAddress("file://#[myPath]");
    assertThat(address.getProtocol(), is("file"));
    assertThat(address.getCredentials(), is(nullValue()));
    assertThat(address.getHost(), is(nullValue()));
    assertThat(address.getPort(), is(nullValue()));
    assertThat(address.getPath(), is("#[myPath]"));
  }

  @Test
  public void httpBasicPath() {
    EndpointAddress address = doProcessAddress("http://host/");
    assertThat(address.getProtocol(), is("http"));
    assertThat(address.getCredentials(), is(nullValue()));
    assertThat(address.getHost(), is("host"));
    assertThat(address.getPort(), is(nullValue()));
    assertThat(address.getPath(), is("/"));
  }

  @Test
  public void httpFullPath() {
    EndpointAddress address = doProcessAddress("http://user:password@host:80/path?param=value&p=3#anchor");
    assertThat(address.getProtocol(), is("http"));
    assertThat(address.getCredentials(), is("user:password"));
    assertThat(address.getHost(), is("host"));
    assertThat(address.getPort(), is("80"));
    assertThat(address.getPath(), is("/path?param=value&p=3#anchor"));
  }

  @Test
  public void httpFullPlaceholdersPath() {
    EndpointAddress address = doProcessAddress("http://${user}:${password}@${host}:${port}/${path}?${params}#${anchor}");
    assertThat(address.getProtocol(), is("http"));
    assertThat(address.getCredentials(), is("${user}:${password}"));
    assertThat(address.getHost(), is("${host}"));
    assertThat(address.getPort(), is("${port}"));
    assertThat(address.getPath(), is("/${path}?${params}#${anchor}"));
  }

  @Test
  public void httpFullExpressionsPath() {
    EndpointAddress address = doProcessAddress("http://#[user]:#[password]@#[host]:#[port]/#[path]?#[params]##[anchor]");
    assertThat(address.getProtocol(), is("http"));
    assertThat(address.getCredentials(), is("#[user]:#[password]"));
    assertThat(address.getHost(), is("#[host]"));
    assertThat(address.getPort(), is("#[port]"));
    assertThat(address.getPath(), is("/#[path]?#[params]##[anchor]"));
  }

  private EndpointAddress doProcessAddress(String address) {
    MigrationReport report = mock(MigrationReport.class);
    doAnswer(invocation -> {
      fail("Couldn't parse address");
      return null;
    }).when(report).report(eq("transports.cantParseAddress"), any(), any());
    return processAddress(new Element("endpoint", COMPATIBILITY_NAMESPACE).setAttribute("address", address),
                          report).get();
  }
}
