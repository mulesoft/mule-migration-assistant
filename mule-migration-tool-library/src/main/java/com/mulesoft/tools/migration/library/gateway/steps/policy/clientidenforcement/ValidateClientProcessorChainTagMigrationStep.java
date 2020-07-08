/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.clientidenforcement;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_TRANSFORM_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_POLICY_NAMESPACE;

import com.mulesoft.tools.migration.library.gateway.steps.policy.ProcessorChainTagMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.policy.utils.ClientIdEnforcementOnErrorContinueElementWriter;

import java.util.List;

import org.jdom2.Content;
import org.jdom2.Element;

/**
 * Migrate error handler client vars
 *
 * @author Mulesoft Inc.
 */
public class ValidateClientProcessorChainTagMigrationStep extends ProcessorChainTagMigrationStep {

  private static final String SET_PAYLOAD_TAG_NAME = "set-payload";
  private static final String ERROR_DESCRIPTION_VALUE = "#[error.description]";
  private static final String VALUE_FLOW_VARS = "#[flowVars._invalidClientMessage]";
  private static final String VALUE_VARS = "#[vars._invalidClientMessage]";

  public ValidateClientProcessorChainTagMigrationStep(String name) {
    super(name);
  }

  private void replaceSetPayloadElement(Element setPayloadElement) {
    if (setPayloadElement != null) {
      setPayloadElement.getAttribute(VALUE_ATTR_NAME).setValue(ERROR_DESCRIPTION_VALUE);
    }
  }

  @Override
  protected void migrateContent(Element errorHandlerElement, List<Content> cloneContentList) {
    Element onErrorContinueElement = new ClientIdEnforcementOnErrorContinueElementWriter().create(errorHandlerElement, false);
    onErrorContinueElement.addContent(0, cloneContentList);
    replaceSetPayloadElement(onErrorContinueElement.getChildren(SET_PAYLOAD_TAG_NAME, MULE_4_POLICY_NAMESPACE).stream()
        .filter(e -> {
          String attrValue = e.getAttributeValue(VALUE_ATTR_NAME);
          return attrValue.equals(VALUE_FLOW_VARS) || attrValue.equals(VALUE_VARS);
        }).findFirst().orElse(null));
    com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorListener.httpListenerLib(getApplicationModel());
    addNamespaceDeclaration(getRootElement(onErrorContinueElement), HTTP_TRANSFORM_NAMESPACE,
                            HTTP_TRANSFORM_XSI_SCHEMA_LOCATION_URI_MULE4);
  }
}
