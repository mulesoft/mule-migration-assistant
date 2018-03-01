/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.step;

import com.mulesoft.tools.migration.engine.MigrationStep;
import com.mulesoft.tools.migration.engine.exception.MigrationStepException;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;

import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;
import static com.mulesoft.tools.migration.report.ReportCategory.SKIPPED;

/**
 * Creates a child node
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CreateChildNode extends MigrationStep {

  private String name;

  public CreateChildNode(String name) {
    setName(name);
  }

  public CreateChildNode() {}

  public void execute() throws Exception {
    try {
      if (!StringUtils.isBlank(name)) {
        for (Element node : getNodes()) {
          if (node.getChild(getName(), node.getNamespace()) != null) {
            //            getReportingStrategy()
            //                .log("<" + node.getChild(getName(), node.getNamespace()).getQualifiedName() + "> node already exists.", SKIPPED,
            //                     this.getDocument().getBaseURI(), null, this);
          } else {
            Element child = new Element(getName());
            child.setNamespace(node.getNamespace());
            node.addContent(child);

            //            getReportingStrategy()
            //                .log("<" + child.getQualifiedName() + "> node was created. Namespace " + child.getNamespaceURI(), RULE_APPLIED,
            //                     this.getDocument().getBaseURI(), null, this);
          }
        }
      }
    } catch (Exception ex) {
      throw new MigrationStepException("Create child node exception. " + ex.getMessage());
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
