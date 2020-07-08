/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_POLICY_NAMESPACE;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Common stuff to migrate filter elements
 *
 * @author Mulesoft Inc.
 */
public abstract class FilterTagMigrationStep extends PolicyMigrationStep {

  public static final String SUB_FLOW_TAG_NAME = "sub-flow";

  public FilterTagMigrationStep(final Namespace namespace, final String tagName) {
    super(namespace, tagName);
  }

  protected Element getProcessorChain(Element root, final String onUnacceptedName) {
    return root.getChildren(SUB_FLOW_TAG_NAME, MULE_4_POLICY_NAMESPACE).stream()
        .filter(element -> element.getAttributeValue(NAME_ATTR_NAME).equals(onUnacceptedName)).findFirst().orElse(null);
  }

  protected boolean hasProcessorChain(Element root, final String onUnacceptedName) {
    return getProcessorChain(root, onUnacceptedName) != null;
  }

  protected abstract void migrateProcessorChain(Element root, final String onUnacceptedName, MigrationReport migrationReport);

}
