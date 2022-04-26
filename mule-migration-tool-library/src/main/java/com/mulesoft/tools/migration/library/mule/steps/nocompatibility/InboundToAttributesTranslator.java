package com.mulesoft.tools.migration.library.mule.steps.nocompatibility;

import com.google.common.collect.ImmutableList;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorListener;

import java.util.List;

public class InboundToAttributesTranslator {
  
  private static List<String> SUPPORTED_SOURCES = ImmutableList.of("listener");

  public static String translate(String originatingSource, String propertyToTranslate) {
    String translation = null;
    if (propertyToTranslate != null) {
      switch (originatingSource) {
        case ("listener"):
          translation =  HttpConnectorListener.inboundToAttributesExpressions().get(propertyToTranslate);
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
