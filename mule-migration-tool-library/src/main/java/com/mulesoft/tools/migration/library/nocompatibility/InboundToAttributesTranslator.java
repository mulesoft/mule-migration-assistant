/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.nocompatibility;

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
import static com.mulesoft.tools.migration.library.applicationgraph.ApplicationGraphCreator.MESSAGE_SOURCE_FILTER_EXPRESSION;

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
import com.mulesoft.tools.migration.project.model.applicationgraph.PropertyTranslator;
import com.mulesoft.tools.migration.project.model.applicationgraph.SourceType;

import com.google.common.collect.Lists;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

  private static String ATTRIBUTES_PATTERN_REGEX = "[a-zA-Z0-9_\\-.'\\[\\]]*";

  private static Map<SourceType, Class> translatorClasses;

  static {
    translatorClasses = new LinkedHashMap<>();
    translatorClasses.put(HTTP_LISTENER, HttpConnectorListener.class);
    translatorClasses.put(HTTP_TRANSPORT, HttpConnectorListener.class);
    translatorClasses.put(HTTP_CONNECTOR_REQUESTER, HttpConnectorRequester.class);
    translatorClasses.put(HTTP_TRANSPORT_OUTBOUND, HttpConnectorRequester.class);
    translatorClasses.put(HTTP_POLLING_CONNECTOR, HttpConnectorRequester.class);
    translatorClasses.put(FILE_INBOUND, FileInboundEndpoint.class);
    translatorClasses.put(IMAP_INBOUND, AbstractEmailSourceMigrator.class);
    translatorClasses.put(POP3_INBOUND, AbstractEmailSourceMigrator.class);
    translatorClasses.put(FTP_INBOUND, FtpInboundEndpoint.class);
    translatorClasses.put(JMS_INBOUND, AbstractJmsEndpoint.class);
    translatorClasses.put(JMS_OUTBOUND, AbstractJmsEndpoint.class);
    translatorClasses.put(REQUEST_REPLY, AbstractJmsEndpoint.class);
    translatorClasses.put(QUARTZ_INBOUND, AbstractJmsEndpoint.class);
    translatorClasses.put(SFTP_INBOUND, SftpInboundEndpoint.class);
    translatorClasses.put(WS_CONSUMER, WsConsumer.class);
  }

  private Map<SourceType, Map<String, String>> applicationTranslations;

  public static List<SourceType> getSupportedConnectors() {
    return Lists.newArrayList(translatorClasses.keySet());
  }

  @Override
  public void initializeTranslationsForApplicationSourceTypes(ApplicationModel applicationModel) {
    if (applicationTranslations == null) {
      Map<SourceType, Map<String, String>> result = new LinkedHashMap<>();
      List<Element> sourceNodes = applicationModel.getNodes("//" + MESSAGE_SOURCE_FILTER_EXPRESSION);
      for (SourceType sourceType : translatorClasses.keySet()) {
        if (sourceNodes.stream().anyMatch(e -> sourceType.equals(new PropertiesSourceType(e.getNamespaceURI(), e.getName())))) {
          result.put(sourceType, getAllTranslationsFor(sourceType));
        }
      }
      applicationTranslations = result;
    }
  }

  @Override
  public Map<SourceType, Map<String, String>> getTranslationsForApplicationsSourceTypes() {
    return applicationTranslations;
  }

  @Override
  public Map<String, String> getAllTranslationsFor(SourceType sourceType) {
    return getTranslationMap(sourceType).entrySet().stream()
        .collect(Collectors.toMap(entry -> entry.getKey(), entry -> wrapWhenExpression(entry.getValue())));
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
        if (propertyToTranslate.contains(".")) {
          translation =
              String.format("%s.%s['%s']", "message.attributes", originatingSourceType.getImplicitPrefix(), propertyToTranslate);
        } else {
          translation =
              String.format("%s.%s.%s", "message.attributes", originatingSourceType.getImplicitPrefix(), propertyToTranslate);
        }
      }
    }
    translation = wrapWhenExpression(translation);

    return translation;
  }


  public Map<String, String> getTranslationMap(SourceType originatingSourceType) {
    Class<?> translatorClazz = translatorClasses.get(originatingSourceType);
    if (translatorClazz != null) {
      try {
        return (Map<String, String>) translatorClazz
            .getMethod("inboundToAttributesExpressions")
            .invoke(null);
      } catch (Exception e) {
        // ignore
      }
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
