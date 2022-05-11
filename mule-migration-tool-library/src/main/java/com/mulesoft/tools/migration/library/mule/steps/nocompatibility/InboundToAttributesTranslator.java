/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.nocompatibility;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mulesoft.tools.migration.library.mule.steps.email.AbstractEmailSourceMigrator;
import com.mulesoft.tools.migration.library.mule.steps.file.FileInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.ftp.FtpInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorListener;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorRequester;
import com.mulesoft.tools.migration.library.mule.steps.jms.AbstractJmsEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.sftp.SftpInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.wsc.WsConsumer;
import com.mulesoft.tools.migration.project.model.applicationgraph.SourceType;

import java.util.List;
import java.util.Map;

import static com.mulesoft.tools.migration.library.mule.steps.email.AbstractEmailMigrator.IMAP_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.email.AbstractEmailMigrator.POP3_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.file.FileConfig.FILE_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.ftp.FtpNamespaceHandler.FTP_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.HTTP_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.jms.AbstractJmsEndpoint.JMS_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.quartz.QuartzInboundEndpoint.QUARTZ_NS_URI;
import static com.mulesoft.tools.migration.library.mule.steps.sftp.AbstractSftpEndpoint.SFTP_NS_URI;
import static com.mulesoft.tools.migration.library.mule.steps.wsc.WsConsumer.WS_NAMESPACE_URI;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NS_URI;

/**
 * Translates between mule 3 inbound properties to mule 4 attributes
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public class InboundToAttributesTranslator {

  public static final SourceType HTTP_LISTENER =
      new SourceType(HTTP_NAMESPACE_URI, "listener");
  public static final SourceType HTTP_TRANSPORT =
      new SourceType(HTTP_NAMESPACE_URI, "inbound-endpoint");
  public static final SourceType HTTP_CONNECTOR_REQUESTER = new SourceType(HTTP_NAMESPACE_URI, "request");
  public static final SourceType HTTP_TRANSPORT_OUTBOUND = new SourceType(HTTP_NAMESPACE_URI, "outbound-endpoint");
  public static final SourceType HTTP_POLLING_CONNECTOR = new SourceType(HTTP_NAMESPACE_URI, "polling-connector");
  public static final SourceType FILE_INBOUND = new SourceType(FILE_NAMESPACE_URI, "listener");
  public static final SourceType IMAP_INBOUND = new SourceType(IMAP_NAMESPACE_URI, "inbound-endpoint");
  public static final SourceType POP3_INBOUND = new SourceType(POP3_NAMESPACE_URI, "inbound-endpoint");
  public static final SourceType FTP_INBOUND = new SourceType(FTP_NAMESPACE_URI, "inbound-endpoint");
  public static final SourceType JMS_INBOUND = new SourceType(JMS_NAMESPACE_URI, "inbound-endpoint");
  public static final SourceType JMS_OUTBOUND = new SourceType(JMS_NAMESPACE_URI, "outbound-endpoint");
  public static final SourceType REQUEST_REPLY =
      new SourceType(CORE_NS_URI, "request-reply");
  public static final SourceType QUARTZ_INBOUND =
      new SourceType(QUARTZ_NS_URI, "inbound-endpoint");
  public static final SourceType SFTP_INBOUND = new SourceType(SFTP_NS_URI, "inbound-endpoint");
  public static final SourceType WS_CONSUMER = new SourceType(WS_NAMESPACE_URI, "consumer");
  private static String ATTRIBUTES_PATTERN_REGEX = "[a-zA-Z0-9_\\-.]*";

  private static Map<SourceType, Class> translatorClasses;

  static {
    translatorClasses = new ImmutableMap.Builder<SourceType, Class>()
        .put(HTTP_LISTENER, HttpConnectorListener.class)
        .put(HTTP_TRANSPORT, HttpConnectorListener.class)
        .put(HTTP_CONNECTOR_REQUESTER, HttpConnectorRequester.class)
        .put(HTTP_TRANSPORT_OUTBOUND, HttpConnectorRequester.class)
        .put(HTTP_POLLING_CONNECTOR, HttpConnectorRequester.class)
        .put(FILE_INBOUND, FileInboundEndpoint.class)
        .put(IMAP_INBOUND, AbstractEmailSourceMigrator.class)
        .put(POP3_INBOUND, AbstractEmailSourceMigrator.class)
        .put(FTP_INBOUND, FtpInboundEndpoint.class)
        .put(JMS_INBOUND, AbstractJmsEndpoint.class)
        .put(JMS_OUTBOUND, AbstractJmsEndpoint.class)
        .put(REQUEST_REPLY, AbstractJmsEndpoint.class)
        .put(QUARTZ_INBOUND, AbstractJmsEndpoint.class)
        .put(SFTP_INBOUND, SftpInboundEndpoint.class)
        .put(WS_CONSUMER, WsConsumer.class)
        .build();
  }

  public static List<SourceType> getSupportedConnectors() {
    return Lists.newArrayList(translatorClasses.keySet());
  }

  public String translate(SourceType originatingSourceType, String propertyToTranslate) throws Exception {
    String translation = null;
    if (propertyToTranslate != null) {
      Class<?> translatorClazz = translatorClasses.get(originatingSourceType);
      if (translatorClazz != null) {
        Map<String, String> translationMap = (Map<String, String>) translatorClazz
            .getMethod("inboundToAttributesExpressions")
            .invoke(null);

        translation = translationMap != null ? translationMap.get(propertyToTranslate) : null;
        // assume is a user defined property
        if (translation == null && isSupported(originatingSourceType)) {
          translation = "message.attributes." + propertyToTranslate;
        }
      }
    }

    translation = wrapWhenExpression(translation);

    return translation;
  }

  private static String wrapWhenExpression(String translation) {

    if (translation != null && !translation.matches(ATTRIBUTES_PATTERN_REGEX)) {
      return String.format("(%s)", translation);
    }

    return translation;
  }

  private static boolean isSupported(SourceType originatingSourceType) {
    return translatorClasses.keySet().contains(originatingSourceType);
  }
}
