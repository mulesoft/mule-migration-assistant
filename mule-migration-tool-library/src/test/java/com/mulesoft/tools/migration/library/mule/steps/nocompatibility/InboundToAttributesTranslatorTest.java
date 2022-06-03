/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.nocompatibility;

import com.mulesoft.tools.migration.library.applicationgraph.PropertiesSourceType;
import com.mulesoft.tools.migration.library.nocompatibility.InboundToAttributesTranslator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Map;

public class InboundToAttributesTranslatorTest {

  private InboundToAttributesTranslator translator;

  @Before
  public void setUp() {
    this.translator = new InboundToAttributesTranslator();
  }

  @Test
  public void testGetAllTranslations_SupportedSimpleTranslation() throws Exception {
    assertEquals("message.attributes.version", translator.getAllTranslationsFor(
                                                                                PropertiesSourceType.HTTP_LISTENER)
        .get().get("http.version"));
    assertEquals("message.attributes.statusCode",
                 translator.getAllTranslationsFor(PropertiesSourceType.HTTP_CONNECTOR_REQUESTER).get().get("http.status"));
    assertEquals("message.attributes.name",
                 translator.getAllTranslationsFor(PropertiesSourceType.FTP_INBOUND).get().get("originalFilename"));
    assertEquals("message.attributes.headers.correlationId",
                 translator.getAllTranslationsFor(PropertiesSourceType.JMS_OUTBOUND).get().get("JMSCorrelationID"));
  }

  @Test
  public void testTranslate_SupportedConnectorCustomProperty() throws Exception {
    assertEquals("message.attributes.headers.myCustomProperty",
                 translator.translateImplicit("myCustomProperty", PropertiesSourceType.HTTP_LISTENER));
  }

  @Test
  public void testTranslate_NonSupportedConnectorTranslation() throws Exception {
    assertNull(translator.translateImplicit("myCustomProperty", new PropertiesSourceType("customUri", "customType")));
  }

  @Test
  public void testGetAllTranslations_SupportedComplexTraslation() throws Exception {
    assertEquals("(message.attributes.requestPath[1 + sizeOf(if (endsWith(message.attributes.listenerPath, '/*')) "
        + "message.attributes.listenerPath[0 to -3] default '/' else message.attributes.listenerPath) to -1])",
                 translator.getAllTranslationsFor(PropertiesSourceType.HTTP_LISTENER).get().get("http.relative.path"));
  }

  @Test
  public void testGetAllTranslationsForAllSourceTypes() throws Exception {
    Map<String, String> allTranslationsForAllSourceTypes = translator.getAllTranslationsForAllSourceTypes();
    assertEquals(47, allTranslationsForAllSourceTypes.size());
  }
}
