/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.nocompatibility;

import static com.mulesoft.tools.migration.library.applicationgraph.ApplicationGraphCreator.MESSAGE_SOURCE_FILTER_EXPRESSION;
import static com.mulesoft.tools.migration.library.applicationgraph.PropertiesSourceType.FILE_INBOUND;
import static com.mulesoft.tools.migration.library.applicationgraph.PropertiesSourceType.FTP_INBOUND;
import static com.mulesoft.tools.migration.library.applicationgraph.PropertiesSourceType.HTTP_CONNECTOR_REQUESTER;
import static com.mulesoft.tools.migration.library.applicationgraph.PropertiesSourceType.HTTP_LISTENER;
import static com.mulesoft.tools.migration.library.applicationgraph.PropertiesSourceType.HTTP_POLLING_CONNECTOR;
import static com.mulesoft.tools.migration.library.applicationgraph.PropertiesSourceType.HTTP_TRANSPORT;
import static com.mulesoft.tools.migration.library.applicationgraph.PropertiesSourceType.HTTP_TRANSPORT_OUTBOUND;
import static com.mulesoft.tools.migration.library.applicationgraph.PropertiesSourceType.IMAP_INBOUND;
import static com.mulesoft.tools.migration.library.applicationgraph.PropertiesSourceType.JMS_INBOUND;
import static com.mulesoft.tools.migration.library.applicationgraph.PropertiesSourceType.JMS_OUTBOUND;
import static com.mulesoft.tools.migration.library.applicationgraph.PropertiesSourceType.POP3_INBOUND;
import static com.mulesoft.tools.migration.library.applicationgraph.PropertiesSourceType.QUARTZ_INBOUND;
import static com.mulesoft.tools.migration.library.applicationgraph.PropertiesSourceType.REQUEST_REPLY;
import static com.mulesoft.tools.migration.library.applicationgraph.PropertiesSourceType.SFTP_INBOUND;
import static com.mulesoft.tools.migration.library.applicationgraph.PropertiesSourceType.WS_CONSUMER;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.library.applicationgraph.PropertiesSourceType;
import com.mulesoft.tools.migration.library.mule.steps.email.AbstractEmailSourceMigrator;
import com.mulesoft.tools.migration.library.mule.steps.file.FileInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.ftp.FtpInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorListener;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorRequester;
import com.mulesoft.tools.migration.library.mule.steps.jms.AbstractJmsEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.sftp.SftpInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.wsc.WsConsumer;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.applicationgraph.SourceType;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.*;

import com.mulesoft.tools.migration.project.model.applicationgraph.PropertyTranslator;

import java.util.function.Function;
import java.util.stream.Collectors;

import org.jdom2.Element;

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

  private Map<SourceType, Map<String, String>> applicationTranslations;

  public static List<SourceType> getSupportedConnectors() {
    return Lists.newArrayList(translatorClasses.keySet());
  }

  @Override
  public void initializeTranslationsForApplicationSourceTypes(ApplicationModel applicationModel) {
    if (applicationTranslations == null) {
      Map<SourceType, Map<String, String>> result = new HashMap<>();
      List<Element> sourceNodes = applicationModel.getNodes("//" + MESSAGE_SOURCE_FILTER_EXPRESSION);
      try {
        for (SourceType sourceType : translatorClasses.keySet()) {
          if (sourceNodes.stream().anyMatch(e -> sourceType.equals(new PropertiesSourceType(e.getNamespaceURI(), e.getName())))) {
            result.put(sourceType, getAllTranslationsFor(sourceType).orElseThrow(NoSuchElementException::new));
          }
        }
      } catch (Exception e) {
        throw new MigrationStepException(e.getMessage(), e);
      }
      applicationTranslations = result;
    }
  }

  @Override
  public Map<SourceType, Map<String, String>> getTranslationsForApplicationsSourceTypes() {
    return applicationTranslations;
  }

  @Override
  public Optional<Map<String, String>> getAllTranslationsFor(SourceType sourceType) throws Exception {
    return Optional.ofNullable(getTranslationMap(sourceType))
        .map(map -> map.entrySet().stream()
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> wrapWhenExpression(entry.getValue()))));
  }

  @Override
  public Map<SourceType, String> translateImplicit(String propertyToTranslate, Set<SourceType> originatingSourceTypes) {
    return originatingSourceTypes.stream()
        .filter(s -> translateImplicit(propertyToTranslate, s) != null)
        .collect(Collectors.toMap(Function.identity(), s -> translateImplicit(propertyToTranslate, s)));
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
