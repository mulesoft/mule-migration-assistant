/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.message;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringJoiner;

/**
 * I knows how to replace properties in a string based on a set of mapping files
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
// TODO rename this class, and re think how it's used
public class MuleMessageUtils {

  private static final String WS_PROPERTIES_PACKAGE = "/message/ws.keys.properties";
  private static final String HTTP_PROPERTIES_PACKAGE = "/message/http.keys.properties";

  // TODO no no
  private static Properties properties;

  private MuleMessageUtils() {}

  public static String replaceContent(String content) throws Exception {
    for (Map.Entry<Object, Object> property : getProperties().entrySet()) {
      content = content.replace(property.getKey().toString(), property.getValue().toString());
      content = content.replace("'" + property.getKey().toString() + "'", property.getValue().toString());
    }
    return content;
  }

  private static Properties getProperties() throws Exception {
    if (properties == null) {
      properties = new Properties();
      loadProperties(properties, HTTP_PROPERTIES_PACKAGE);
      loadProperties(properties, WS_PROPERTIES_PACKAGE);
    }
    return properties;
  }

  private static void loadProperties(Properties properties, String path) throws Exception {
    InputStream in = MuleMessageUtils.class.getResourceAsStream(path);
    properties.load(in);
    in.close();
  }
}
