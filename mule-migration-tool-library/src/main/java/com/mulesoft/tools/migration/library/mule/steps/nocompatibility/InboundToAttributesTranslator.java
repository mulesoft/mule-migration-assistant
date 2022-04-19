package com.mulesoft.tools.migration.library.mule.steps.nocompatibility;

import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorListener;

import java.util.List;

public class InboundToAttributesTranslator {

  public static String translate(List<String> originatingSources, String propertyToTranslate) {
    String translation = null;
    int i = 0;
    while (translation == null && i < originatingSources.size()) {
      translation = tryTranslate(originatingSources.get(i), propertyToTranslate);
      i++;
    }
    return translation;
  }

  private static String tryTranslate(String originatingSource, String propertyToTranslate) {
    switch (originatingSource) {
      case ("http:listener"):
        return HttpConnectorListener.inboundToAttributesExpressions().get(propertyToTranslate);
      default:
        return null;
    }
  }
}
