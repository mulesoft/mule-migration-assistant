/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.basicstructure;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_CORE_NAMESPACE_NO_PREFIX;

import com.mulesoft.tools.migration.library.gateway.steps.policy.PolicyMigrationStep;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Common stuff for migrators of Policy elements
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractBasicStructureMigrationStep extends PolicyMigrationStep {

  protected static final String MULE_3_TAG_NAME = "policy";

  public AbstractBasicStructureMigrationStep(final Namespace namespace, final String tagName) {
    super(namespace, tagName);
  }

  protected final List<Content> detachContentClonning(final List<Content> contentList) {
    final List<Content> result = new ArrayList<>();
    final int contentListSize = contentList.size();
    for (int i = 0; i < contentListSize; i++) {
      Content c = contentList.get(0);
      result.add(c.clone());
      c.detach();
    }
    return result;
  }

  protected void replaceNamespace(final List<Content> cloneContentList) {
    for (final Content content : cloneContentList) {
      if (content instanceof Element) {
        Element e = (Element) content;
        final String namespacePrefix = e.getNamespacePrefix();
        if (namespacePrefix.isEmpty() || namespacePrefix.equals(MULE_4_TAG_NAME)) {
          e.setNamespace(MULE_4_CORE_NAMESPACE_NO_PREFIX);
        }
      }
    }
  }

}
