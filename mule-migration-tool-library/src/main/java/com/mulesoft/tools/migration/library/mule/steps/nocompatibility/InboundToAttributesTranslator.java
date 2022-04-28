package com.mulesoft.tools.migration.library.mule.steps.nocompatibility;

import com.google.common.collect.ImmutableList;
import com.mulesoft.tools.migration.library.mule.steps.file.FileInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorListener;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorRequester;

import java.util.List;

public class InboundToAttributesTranslator {
  
  private static List<String> SUPPORTED_SOURCES = ImmutableList.of("http:listener", "file:listener", "http:request");

  public static String translate(String originatingSource, String propertyToTranslate) {
    String translation = null;
    if (propertyToTranslate != null) {
      switch (originatingSource) {
        case ("http:listener"):
          translation =  HttpConnectorListener.inboundToAttributesExpressions().get(propertyToTranslate);
          break;
        case ("file:listener"):
          translation = FileInboundEndpoint.inboundToAttributesExpressions().get(propertyToTranslate);
          break;
        case ("http:request"):
          translation = HttpConnectorRequester.inboundToAttributesExpressions().get(propertyToTranslate);
          break;
        default:
          translation = null;
      }
      
      // assume is a user defined property
      if (translation == null && isSupported(originatingSource)) {
        translation = "message.attributes." + propertyToTranslate;     
      }
    }
    
    return translation;
  }
  
  private static boolean isSupported(String originatingSource) {
    return SUPPORTED_SOURCES.contains(originatingSource);  
  }
}
