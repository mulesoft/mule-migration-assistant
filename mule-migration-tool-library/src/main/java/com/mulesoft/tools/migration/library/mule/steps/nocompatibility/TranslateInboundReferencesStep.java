/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.nocompatibility;

import com.mulesoft.tools.migration.project.model.applicationgraph.*;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Element;

import java.util.*;

import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NAMESPACE;

/**
 * Step to translate inbound property references
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public class TranslateInboundReferencesStep extends AbstractApplicationModelMigrationStep {

  public TranslateInboundReferencesStep() {
    this.setAppliedTo("*");
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public void execute(Element unused, MigrationReport report) throws RuntimeException {
    // only works if "no compatibility mode" is on, which means the application graph exists
    if (getApplicationModel().getApplicationGraph() != null) {
      ApplicationGraph applicationGraph = getApplicationModel().getApplicationGraph();

      applicationGraph.getAllStartingFlowComponents().stream()
          .map(startingPoint -> applicationGraph.getAllFlowComponentsOfTypeAlongPath(startingPoint, MessageProcessor.class,
                                                                                     COMPATIBILITY_NAMESPACE.getPrefix() + '_'
                                                                                         + "attributes-to-inbound-properties"))
          .flatMap(Collection::stream)
          .forEach(mp -> mp.getXmlElement().detach());
    }
  }

  @Override
  public boolean shouldReportMetrics() {
    return false;
  }
}
