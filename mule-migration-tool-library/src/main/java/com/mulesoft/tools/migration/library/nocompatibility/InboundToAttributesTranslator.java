/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.nocompatibility;

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
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mulesoft.tools.migration.library.applicationgraph.PropertiesSourceType.*;

/**
 * Translates between mule 3 inbound properties to mule 4 attributes
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public class InboundToAttributesTranslator implements PropertyTranslator {

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

  @Override
  public Optional<Map<String, String>> getAllTranslationsFor(SourceType sourceType) throws Exception {
    return Optional.ofNullable(getTranslationMap(sourceType))
        .map(map -> map.entrySet().stream()
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> wrapWhenExpression(entry.getValue()))));
  }

  @Override
  public String translateImplicit(String propertyToTranslate, SourceType originatingSourceType) {
    String translation = null;
    if (propertyToTranslate != null) {
      if (isSupported(originatingSourceType) && originatingSourceType.supportsImplicit()) {
        translation =
            String.format("%s.%s.%s", "message.attributes", originatingSourceType.getImplicitPrefix(), propertyToTranslate);
      }
    }
    translation = wrapWhenExpression(translation);

    return translation;
  }

  public Map<String, String> getTranslationMap(SourceType originatingSourceType) throws Exception {
    Class<?> translatorClazz = translatorClasses.get(originatingSourceType);
    if (translatorClazz != null) {
      return (Map<String, String>) translatorClazz
          .getMethod("inboundToAttributesExpressions")
          .invoke(null);
    }
    return null;
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
