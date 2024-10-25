/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.ipfilter;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_TRANSFORM_NAMESPACE;

import com.mulesoft.tools.migration.library.gateway.steps.policy.ProcessorChainTagMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.policy.utils.IpFilterOnErrorContinueElementWriter;

import java.util.List;

import org.jdom2.Content;
import org.jdom2.Element;

/**
 * Migrate ip filters error handler
 *
 * @author Mulesoft Inc.
 */
public class IpFilterProcessorChainTagMigrationStep extends ProcessorChainTagMigrationStep {

  public IpFilterProcessorChainTagMigrationStep(String name) {
    super(name);
  }

  @Override
  protected void migrateContent(Element errorHandlerElement, final List<Content> cloneContentList) {
    Element onErrorContinueElement = new IpFilterOnErrorContinueElementWriter().create(errorHandlerElement, false);
    onErrorContinueElement.addContent(0, cloneContentList);
    com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorListener.httpListenerLib(getApplicationModel());
    addNamespaceDeclaration(getRootElement(onErrorContinueElement), HTTP_TRANSFORM_NAMESPACE,
                            HTTP_TRANSFORM_XSI_SCHEMA_LOCATION_URI_MULE4);
  }

}
