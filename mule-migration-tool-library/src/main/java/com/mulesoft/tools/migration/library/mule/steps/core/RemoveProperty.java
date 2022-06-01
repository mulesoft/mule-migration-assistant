/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeAttribute;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;
import static com.mulesoft.tools.migration.project.model.applicationgraph.SetPropertyProcessor.OUTBOUND_PREFIX;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.*;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.mulesoft.tools.migration.project.model.applicationgraph.RemovePropertyProcessor;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate Remove Property to the compatibility plugin
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RemoveProperty extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("remove-property");

  @Override
  public String getDescription() {
    return "Update Remove Property namespace to compatibility.";
  }

  public RemoveProperty() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(COMPATIBILITY_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    if (getApplicationModel().getApplicationGraph() != null) {
      String propertyName = element.getAttributeValue("propertyName");
      RemovePropertyProcessor processor = (RemovePropertyProcessor) getApplicationModel()
          .getApplicationGraph().findFlowComponent(element);
      String variableNameTranslation = processor.getPropertiesMigrationContext().getOutboundContext()
          .get(propertyName).getTranslation();
      if (variableNameTranslation != null) {
        variableNameTranslation = variableNameTranslation.replace("vars.", "");
        changeNodeName("", "remove-variable")
            .andThen(changeAttribute("propertyName", of("variableName"), of(variableNameTranslation)))
            .apply(element);
      }
    } else {
      addCompatibilityNamespace(element.getDocument());
      report.report("message.outboundProperties", element, element);
      element.setNamespace(COMPATIBILITY_NAMESPACE);
    }
  }
}
