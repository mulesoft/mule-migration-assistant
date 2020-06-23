/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsArrayContaining.hasItemInArray;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mulesoft.tools.migration.library.mule.steps.core.AttributesToInboundPropertiesScriptGenerator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HttpListenerAttributesMappingTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path HTTP_LISTENER_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/http");

  private HttpConnectorListener httpListener;
  private AttributesToInboundPropertiesScriptGenerator a2ipScriptGenerator;

  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    httpListener = new HttpConnectorListener();
    appModel = mock(ApplicationModel.class);
    when(appModel.getProjectBasePath()).thenReturn(temp.newFolder().toPath());
    httpListener.setApplicationModel(appModel);

    a2ipScriptGenerator = new AttributesToInboundPropertiesScriptGenerator();
  }

  @Test
  public void execute() throws Exception {
    Document doc =
        getDocument(this.getClass().getClassLoader()
            .getResource(HTTP_LISTENER_CONFIG_EXAMPLES_PATH.resolve("http-listener-01-original.xml").toString()).toURI()
            .getPath());


    getElementsFromDocument(doc, httpListener.getAppliedTo().getExpression())
        .forEach(node -> httpListener.execute(node, report.getReport()));

    a2ipScriptGenerator.execute(appModel.getProjectBasePath(), report.getReport());

    File migrationFolder = new File(appModel.getProjectBasePath().toFile(), "src/main/resources/migration");

    assertThat(migrationFolder.listFiles(), arrayWithSize(greaterThanOrEqualTo(1)));
    assertThat(migrationFolder.list(), hasItemInArray("attributes2inboundProperties.dwl"));

    assertThat(IOUtils.toString(new File(migrationFolder, "attributes2inboundProperties.dwl").toURI(), UTF_8), is(""
        + "%dw 2.0" + lineSeparator() +
        "output application/java" + lineSeparator() +
        " ---" + lineSeparator() +
        "if (message.attributes.^class == 'org.mule.extension.http.api.HttpRequestAttributes')" + lineSeparator() +
        "{" + lineSeparator() +
        "    'http.listener.path': message.attributes.listenerPath," + lineSeparator() +
        "    'http.context.path': if (endsWith(message.attributes.listenerPath, '/*')) message.attributes.listenerPath[0 to -3] default '/' else message.attributes.listenerPath,"
        + lineSeparator() +
        "    'http.relative.path': message.attributes.requestPath[1 + sizeOf(if (endsWith(message.attributes.listenerPath, '/*')) message.attributes.listenerPath[0 to -3] default '/' else message.attributes.listenerPath) to -1],"
        + lineSeparator() +
        "    'http.version': message.attributes.version," + lineSeparator() +
        "    'http.scheme': message.attributes.scheme," + lineSeparator() +
        "    'http.method': message.attributes.method," + lineSeparator() +
        "    'http.request.uri': message.attributes.requestUri," + lineSeparator() +
        "    'http.query.string': message.attributes.queryString," + lineSeparator() +
        "    'http.remote.address': message.attributes.remoteAddress," + lineSeparator() +
        "    'http.client.cert': message.attributes.clientCertificate," + lineSeparator() +
        "    'LOCAL_CERTIFICATES': [message.attributes.clientCertificate]," + lineSeparator() +
        "    'PEER_CERTIFICATES': [message.attributes.clientCertificate]," + lineSeparator() +
        "    'http.query.params': message.attributes.queryParams," + lineSeparator() +
        "    'http.uri.params': message.attributes.uriParams," + lineSeparator() +
        "    'http.request': message.attributes.requestPath," + lineSeparator() +
        "    'http.request.path': message.attributes.requestPath," + lineSeparator() +
        "    'http.headers': message.attributes.headers" + lineSeparator() +
        "}" + lineSeparator() +
        " ++ message.attributes.headers mapObject ((value, key, index) -> { (if(upper(key as String) startsWith 'X-MULE_') upper((key as String) [2 to -1]) else key) : value })"
        + lineSeparator() +
        " ++ message.attributes.queryParams" + lineSeparator() +
        "else" + lineSeparator() +
        "{}" + lineSeparator()));
  }
}
