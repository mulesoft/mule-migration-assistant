/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.nocompatibility;

import com.mulesoft.tools.migration.project.model.applicationgraph.SourceType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class InboundToAttributesTranslatorTest {

  private InboundToAttributesTranslator translator;

  @Before
  public void setUp() {
    this.translator = new InboundToAttributesTranslator();
  }

  @Test
  public void testTranslate_SupportedSimpleTranslation() throws Exception {
    assertEquals("message.attributes.version", translator.translate(InboundToAttributesTranslator.HTTP_LISTENER,
                                                                    "http.version"));
    assertEquals("message.attributes.statusCode", translator.translate(InboundToAttributesTranslator.HTTP_CONNECTOR_REQUESTER,
                                                                       "http.status"));
    assertEquals("message.attributes.name", translator.translate(InboundToAttributesTranslator.FTP_INBOUND,
                                                                 "originalFilename"));
    assertEquals("message.attributes.headers.correlationId", translator.translate(InboundToAttributesTranslator.JMS_OUTBOUND,
                                                                                  "JMSCorrelationID"));
  }

  @Test
  public void testTranslate_SupportedConnectorCustomProperty() throws Exception {
    assertEquals("message.attributes.myCustomProperty", translator.translate(InboundToAttributesTranslator.HTTP_LISTENER,
                                                                             "myCustomProperty"));
  }

  @Test
  public void testTranslate_NonSupportedConnectorTranslation() throws Exception {
    assertNull(translator.translate(new SourceType("customUri", "customType"),
                                    "myCustomProperty"));
  }

  @Test
  public void testTranslate_SupportedComplexTraslation() throws Exception {
    assertEquals("(message.attributes.requestPath[1 + sizeOf(if (endsWith(message.attributes.listenerPath, '/*')) "
        + "message.attributes.listenerPath[0 to -3] default '/' else message.attributes.listenerPath) to -1])",
                 translator.translate(InboundToAttributesTranslator.HTTP_LISTENER, "http.relative.path"));
  }

}
