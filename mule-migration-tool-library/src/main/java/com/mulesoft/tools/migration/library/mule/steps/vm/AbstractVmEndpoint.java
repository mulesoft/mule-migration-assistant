/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.mulesoft.tools.migration.library.mule.steps.vm;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.setText;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.Optional;


/**
 * Migrates the endpoints of the VM Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractVmEndpoint extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  protected static final String VM_NAMESPACE_PREFIX = "vm";
  protected static final String VM_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/vm";

  public static final String VM_SCHEMA_LOCATION = "http://www.mulesoft.org/schema/mule/vm/current/mule-vm.xsd";
  public static final Namespace VM_NAMESPACE = getNamespace(VM_NAMESPACE_PREFIX, VM_NAMESPACE_URI);

  private ExpressionMigrator expressionMigrator;

  protected static String obtainPath(Element object) {
    String path = object.getAttributeValue("path");

    if (path.contains("?")) {
      String[] splitPath = path.split("\\?");
      path = splitPath[0];

      for (String urlParam : splitPath[1].split("&")) {
        String[] splitUrlParam = urlParam.split("=");

        String key = splitUrlParam[0];
        String value = splitUrlParam[1];

        if ("responseTransformers".equals(key)) {
          object.setAttribute("responseTransformer-refs", value);
        } else {
          object.setAttribute(key, value);
        }
      }
    }
    return path;
  }

  protected static void addQueue(final Namespace vmConnectorNamespace, Optional<Element> connector, Element vmConfig,
                                 String path) {
    Element queues = vmConfig.getChild("queues", vmConnectorNamespace);
    if (!queues.getChildren().stream().filter(e -> path.equals(e.getAttributeValue("queueName"))).findAny().isPresent()) {
      Element queue = new Element("queue", vmConnectorNamespace);
      queue.setAttribute("queueName", path);

      queue.setAttribute("queueType", "TRANSIENT");
      connector.ifPresent(conn -> {
        Optional<Element> queueProfile = empty();
        if (conn.getChild("queueProfile", VM_NAMESPACE) != null) {
          queueProfile = of(conn.getChild("queueProfile", VM_NAMESPACE));
        } else if (conn.getChild("queue-profile", VM_NAMESPACE) != null) {
          queueProfile = of(conn.getChild("queue-profile", VM_NAMESPACE));
        }

        if ("true".equals(queueProfile.map(qp -> qp.getAttributeValue("persistent")).orElse(null))) {
          queue.setAttribute("queueType", "PERSISTENT");
        }
        if (queueProfile.map(qp -> qp.getAttribute("maxOutstandingMessages")).orElse(null) != null) {
          queue.setAttribute("maxOutstandingMessages", queueProfile.get().getAttributeValue("maxOutstandingMessages"));
        }
      });
      queues.addContent(queue);
    }
  }

  protected static Element buildContent(final Namespace vmConnectorNamespace) {
    // TODO MMT-166 Use something that includes the extra parameters in the media type, instead of ^mimeType
    // (https://github.com/mulesoft/data-weave/issues/296)
    return setText(new Element("content", vmConnectorNamespace),
                   "#[output application/java --- {'_vmTransportMode': true, 'payload': payload.^raw, 'mimeType': payload.^mimeType, 'session': vars.compatibility_outboundProperties['MULE_SESSION']}]");
  }

  public static Optional<Element> resolveVmConector(Element object, ApplicationModel appModel) {
    Optional<Element> connector;
    if (object.getAttribute("connector-ref") != null) {
      connector = of(getConnector(object.getAttributeValue("connector-ref"), appModel));
      object.removeAttribute("connector-ref");
    } else {
      connector = getDefaultConnector(appModel);
    }
    return connector;
  }

  protected static Element getConnector(String connectorName, ApplicationModel appModel) {
    return appModel.getNode("/*/*[namespace-uri()='" + VM_NAMESPACE_URI + "' and local-name()='connector' and @name = '"
        + connectorName + "']");
  }

  protected static Optional<Element> getDefaultConnector(ApplicationModel appModel) {
    return appModel.getNodeOptional("/*/*[namespace-uri()='" + VM_NAMESPACE_URI + "' and local-name()='connector']");
  }

  public static String getVmConfigName(Element object, Optional<Element> connector) {
    String configName = connector.map(conn -> conn.getAttributeValue("name")).orElse((object.getAttribute("name") != null
        ? object.getAttributeValue("name")
        : (object.getAttribute("ref") != null
            ? object.getAttributeValue("ref")
            : "")).replaceAll("\\\\", "_")
        + "VmConfig");
    return configName;
  }

  public static Element migrateVmConfig(Element object, Optional<Element> connector, String configName,
                                        ApplicationModel appModel) {
    Optional<Element> config = appModel.getNodeOptional("*/*[namespace-uri()='" + VM_NAMESPACE_URI
        + "' and local-name()='config' and @name='" + configName + "']");
    Element vmConfig = config.orElseGet(() -> {
      Element vmCfg = new Element("config", VM_NAMESPACE);
      vmCfg.setAttribute("name", configName);
      Element queues = new Element("queues", VM_NAMESPACE);
      vmCfg.addContent(queues);

      addTopLevelElement(vmCfg, connector.map(c -> c.getDocument()).orElse(object.getDocument()));

      return vmCfg;
    });
    return vmConfig;
  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }
}
