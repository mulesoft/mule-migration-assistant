/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.basicstructure;


import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_3_POLICY_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_CORE_NAMESPACE_NO_PREFIX;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_POLICY_NAMESPACE;

import com.mulesoft.tools.migration.library.gateway.steps.policy.PolicyMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.List;

import org.jdom2.Content;
import org.jdom2.Element;

/**
 * Migrate after-exception element
 *
 * @author Mulesoft Inc.
 */
public class AfterExceptionTagMigrationStep extends AbstractBasicStructureMigrationStep {

  private static final String AFTER_EXCEPTION_TAG_NAME = "after-exception";
  private static final String ON_ERROR_CONTINUE = "on-error-continue";

  public AfterExceptionTagMigrationStep() {
    super(MULE_3_POLICY_NAMESPACE, AFTER_EXCEPTION_TAG_NAME);
  }

  private Element getErrorHandlerElement(final List<Content> elementCloneContentList) {
    Element onErrorContinue = new Element(ON_ERROR_CONTINUE, MULE_4_POLICY_NAMESPACE);
    onErrorContinue.addContent(elementCloneContentList);
    return new Element(PolicyMigrationStep.ERROR_HANDLER_TAG_NAME, MULE_4_POLICY_NAMESPACE).addContent(onErrorContinue);
  }

  private void completeTryElement(Element source, List<Content> elementCloneContentList) {
    Element tryElement = source.getChild(PolicyMigrationStep.TRY_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
    if (tryElement.getChild(PolicyMigrationStep.ERROR_HANDLER_TAG_NAME, MULE_4_POLICY_NAMESPACE) == null) {
      tryElement.addContent(getErrorHandlerElement(elementCloneContentList));
    } else {
      Element errorHandlerElement = tryElement.getChild(PolicyMigrationStep.ERROR_HANDLER_TAG_NAME, MULE_4_POLICY_NAMESPACE);
      errorHandlerElement.addContent(elementCloneContentList);
      replaceNamespace(errorHandlerElement.getContent());
    }
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    final List<Content> elementCloneContentList = detachContent(element.getContent());
    Element source = setUpHttpPolicy(element, true, migrationReport);
    if (source.getChild(PolicyMigrationStep.TRY_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX) == null) {
      final List<Content> sourceCloneContentList = detachContent(source.getContent());
      Element tryElement = new Element(PolicyMigrationStep.TRY_TAG_NAME, MULE_4_POLICY_NAMESPACE);
      source.addContent(tryElement);
      replaceNamespace(elementCloneContentList);
      tryElement.addContent(sourceCloneContentList);
      tryElement.addContent(getErrorHandlerElement(elementCloneContentList));
    } else {
      completeTryElement(source, elementCloneContentList);
    }
  }

}
