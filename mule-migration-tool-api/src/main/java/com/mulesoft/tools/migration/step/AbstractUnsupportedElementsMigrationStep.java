/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.step;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.util.XmlDslUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Takes unsupported or blocked elements and comments them, mark them as failed
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractUnsupportedElementsMigrationStep extends AbstractApplicationModelMigrationStep {

  public static final String COMPONENTS_UNSUPPORTED_ERROR_KEY = "components.unsupported";
  public static final String NAME_ATTRIBUTE_TEMPLATE = "(name = %s)";

  public AbstractUnsupportedElementsMigrationStep(Namespace namespace) {
    checkArgument(getUnsupportedElements() != null, "The unsupported elements list must not be null.");
    this.setAppliedTo(XmlDslUtils.getAllElementsFromNamespaceXpathSelector(namespace.getURI(), false));
    this.setNamespacesContributions(newArrayList(namespace));
  }

  public abstract List<String> getUnsupportedElements();

  @Override
  public void execute(Element node, MigrationReport report) throws RuntimeException {
    if (getUnsupportedElements().contains(node.getName())) {
      report.report(COMPONENTS_UNSUPPORTED_ERROR_KEY, node, node.getParentElement(),
                    report.getComponentKey(node) + " " + String.format(NAME_ATTRIBUTE_TEMPLATE, node.getAttributeValue("name")));
      node.getParentElement().removeChild(node.getName(), node.getNamespace());
    }
  }
}
