/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.applicationgraph;

import static com.mulesoft.tools.migration.library.mule.steps.email.AbstractEmailMigrator.IMAP_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.email.AbstractEmailMigrator.POP3_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.file.FileConfig.FILE_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.ftp.FtpNamespaceHandler.FTP_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.jms.AbstractJmsEndpoint.JMS_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.quartz.QuartzInboundEndpoint.QUARTZ_NS_URI;
import static com.mulesoft.tools.migration.library.mule.steps.sftp.AbstractSftpEndpoint.SFTP_NS_URI;
import static com.mulesoft.tools.migration.library.mule.steps.wsc.WsConsumer.WS_NAMESPACE_URI;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NS_URI;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.HTTP_NAMESPACE_URI;

import com.mulesoft.tools.migration.project.model.applicationgraph.SourceType;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

/**
 * Models an inbound properties source type
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public class PropertiesSourceType implements SourceType, Comparable<PropertiesSourceType> {

  public static final PropertiesSourceType HTTP_LISTENER =
      new PropertiesSourceType(HTTP_NAMESPACE_URI, "listener", true, "headers", true);
  public static final PropertiesSourceType HTTP_TRANSPORT =
      new PropertiesSourceType(HTTP_NAMESPACE_URI, "inbound-endpoint", true, "headers", true);
  public static final PropertiesSourceType HTTP_CONNECTOR_REQUESTER =
      new PropertiesSourceType(HTTP_NAMESPACE_URI, "request", true, "headers", false);
  public static final PropertiesSourceType HTTP_TRANSPORT_OUTBOUND =
      new PropertiesSourceType(HTTP_NAMESPACE_URI, "outbound-endpoint", false);
  public static final PropertiesSourceType HTTP_POLLING_CONNECTOR =
      new PropertiesSourceType(HTTP_NAMESPACE_URI, "polling-connector", true);
  public static final PropertiesSourceType FILE_INBOUND = new PropertiesSourceType(FILE_NAMESPACE_URI, "inbound-endpoint", true);
  public static final PropertiesSourceType IMAP_INBOUND = new PropertiesSourceType(IMAP_NAMESPACE_URI, "inbound-endpoint", true);
  public static final PropertiesSourceType POP3_INBOUND = new PropertiesSourceType(POP3_NAMESPACE_URI, "inbound-endpoint", true);
  public static final PropertiesSourceType FTP_INBOUND = new PropertiesSourceType(FTP_NAMESPACE_URI, "inbound-endpoint", true);
  public static final PropertiesSourceType JMS_INBOUND = new PropertiesSourceType(JMS_NAMESPACE_URI, "inbound-endpoint", true);
  public static final PropertiesSourceType JMS_OUTBOUND = new PropertiesSourceType(JMS_NAMESPACE_URI, "outbound-endpoint", false);
  public static final PropertiesSourceType REQUEST_REPLY =
      new PropertiesSourceType(CORE_NS_URI, "request-reply", false);
  public static final PropertiesSourceType QUARTZ_INBOUND =
      new PropertiesSourceType(QUARTZ_NS_URI, "inbound-endpoint", true);
  public static final PropertiesSourceType SFTP_INBOUND = new PropertiesSourceType(SFTP_NS_URI, "inbound-endpoint", true);
  public static final PropertiesSourceType WS_CONSUMER = new PropertiesSourceType(WS_NAMESPACE_URI, "consumer", false);

  public static List<PropertiesSourceType> registeredSourceTypes = new ImmutableList.Builder<PropertiesSourceType>()
      .add(HTTP_LISTENER)
      .add(HTTP_TRANSPORT)
      .add(HTTP_CONNECTOR_REQUESTER)
      .add(HTTP_TRANSPORT_OUTBOUND)
      .add(HTTP_POLLING_CONNECTOR)
      .add(FILE_INBOUND)
      .add(IMAP_INBOUND)
      .add(POP3_INBOUND)
      .add(FTP_INBOUND)
      .add(JMS_INBOUND)
      .add(JMS_OUTBOUND)
      .add(REQUEST_REPLY)
      .add(QUARTZ_INBOUND)
      .add(SFTP_INBOUND)
      .add(WS_CONSUMER)
      .build();

  public static PropertiesSourceType getRegistered(String namespaceUri, String name) {
    return registeredSourceTypes.stream().filter(registered -> new PropertiesSourceType(namespaceUri, name).equals(registered))
        .findFirst().orElse(null);
  }

  private String namespaceUri;
  private String type;
  private boolean supportsImplicit;
  private String implicitPrefix = null;
  private boolean isFlowSource;

  public PropertiesSourceType(String namespaceUri, String type) {
    this.namespaceUri = namespaceUri;
    this.type = type;
    this.supportsImplicit = false;
  }

  public PropertiesSourceType(String namespaceUri, String type, boolean isFlowSource) {
    this.namespaceUri = namespaceUri;
    this.type = type;
    this.supportsImplicit = false;
    this.isFlowSource = isFlowSource;
  }

  private PropertiesSourceType(String namespaceUri, String type, boolean supportsImplicit, String implicitPrefix,
                               boolean isFlowSource) {
    this.namespaceUri = namespaceUri;
    this.type = type;
    this.supportsImplicit = supportsImplicit;
    this.implicitPrefix = implicitPrefix;
    this.isFlowSource = isFlowSource;
  }

  public String getNamespaceUri() {
    return namespaceUri;
  }

  public String getType() {
    return type;
  }

  public boolean supportsImplicit() {
    return supportsImplicit;
  }

  public String getImplicitPrefix() {
    return implicitPrefix;
  }

  @Override
  public boolean isFlowSource() {
    return isFlowSource;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    PropertiesSourceType that = (PropertiesSourceType) o;
    return Objects.equals(namespaceUri, that.namespaceUri)
        && Objects.equals(type, that.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(namespaceUri, type);
  }

  @Override
  public String toString() {
    return String.format("%s:%s", namespaceUri.substring(namespaceUri.lastIndexOf("/") + 1), type);
  }

  @Override
  public int compareTo(PropertiesSourceType o) {
    return registeredSourceTypes.indexOf(this) - registeredSourceTypes.indexOf(o);
  }
}
