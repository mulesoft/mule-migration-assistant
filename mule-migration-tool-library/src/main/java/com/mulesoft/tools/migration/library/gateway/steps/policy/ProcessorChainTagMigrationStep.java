/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_CORE_NAMESPACE_NO_PREFIX;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_POLICY_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.policy.FilterTagMigrationStep.SUB_FLOW_TAG_NAME;
import static java.util.Arrays.asList;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.List;

import org.jdom2.Content;
import org.jdom2.Element;

/**
 * Common stuff to migrate headers elements
 *
 * @author Mulesoft Inc.
 */
public abstract class ProcessorChainTagMigrationStep extends PolicyMigrationStep {

  protected static final String HEADERS_TAG_NAME = "headers";
  protected static final String HTTP_TRANSFORM_XSI_SCHEMA_LOCATION_URI_MULE4 =
      "http://www.mulesoft.org/schema/mule/http-policy-transform http://www.mulesoft.org/schema/mule/http-policy-transform/current/mule-http-policy-transform.xsd";

  public ProcessorChainTagMigrationStep(final String name) {
    this.setNamespacesContributions(asList(MULE_4_POLICY_NAMESPACE));
    this.setAppliedTo(getXPathSelector(MULE_4_POLICY_NAMESPACE, SUB_FLOW_TAG_NAME, NAME_ATTR_NAME, name));
  }

  protected abstract void migrateContent(Element element, final List<Content> cloneContentList);

  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    final List<Content> cloneContentList = detachContent(element.getContent());
    Element source = setUpHttpPolicy(element, true, migrationReport);
    Element tryElement = source.getChild(TRY_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
    if (tryElement == null) {
      final List<Content> sourceCloneContentList = detachContent(source.getContent());
      tryElement = new Element(TRY_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
      source.addContent(tryElement);
      sourceCloneContentList.forEach(tryElement::addContent);
    }
    Element errorHandlerElement = tryElement.getChild(ERROR_HANDLER_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
    if (errorHandlerElement == null) {
      errorHandlerElement = new Element(ERROR_HANDLER_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
      tryElement.addContent(errorHandlerElement);
    }
    migrateContent(errorHandlerElement, cloneContentList);
  }
}
