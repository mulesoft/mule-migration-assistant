/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.nocompatibility;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mulesoft.tools.migration.library.applicationgraph.PropertiesSourceType;
import com.mulesoft.tools.migration.library.nocompatibility.InboundToAttributesTranslator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

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
        .get("http.version"));
    assertEquals("message.attributes.statusCode",
                 translator.getAllTranslationsFor(PropertiesSourceType.HTTP_CONNECTOR_REQUESTER).get("http.status"));
    assertEquals("message.attributes.name",
                 translator.getAllTranslationsFor(PropertiesSourceType.FTP_INBOUND).get("originalFilename"));
    assertEquals("message.attributes.headers.correlationId",
                 translator.getAllTranslationsFor(PropertiesSourceType.JMS_OUTBOUND).get("JMSCorrelationID"));
  }

  @Test
  public void testTranslate_SupportedConnectorCustomProperty() throws Exception {
    assertEquals(ImmutableMap.of(PropertiesSourceType.HTTP_LISTENER, "message.attributes.headers.myCustomProperty"),
                 translator.translateImplicit("myCustomProperty", Sets.newHashSet(PropertiesSourceType.HTTP_LISTENER)));
  }

  @Test
  public void testTranslate_NonSupportedConnectorTranslation() throws Exception {
    assertTrue(translator.translateImplicit("myCustomProperty",
                                            Sets.newHashSet(new PropertiesSourceType("customUri", "customType")))
        .isEmpty());
  }

  @Test
  public void testGetAllTranslations_SupportedComplexTraslation() throws Exception {
    assertEquals("(message.attributes.requestPath[1 + sizeOf(if (endsWith(message.attributes.listenerPath, '/*')) "
        + "message.attributes.listenerPath[0 to -3] default '/' else message.attributes.listenerPath) to -1])",
                 translator.getAllTranslationsFor(PropertiesSourceType.HTTP_LISTENER).get("http.relative.path"));
  }

}
