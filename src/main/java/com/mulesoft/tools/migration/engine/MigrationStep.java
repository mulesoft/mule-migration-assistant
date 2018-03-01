/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import com.mulesoft.tools.migration.project.model.ApplicationModel;

/**
 * Basic unit of execution
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class MigrationStep implements Executable {

  private String description;
  private ApplicationModel applicationModel;

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String descriptor) {
    this.description = descriptor;
  }

  public ApplicationModel getApplicationModel() {
    return applicationModel;
  }

  public void setApplicationModel(ApplicationModel applicationModel) {
    checkArgument(applicationModel != null, "The application model must not be null.");
    this.applicationModel = applicationModel;
  }

  @Deprecated
  public List<Element> getNodes() {
    return null;
  }

  @Deprecated
  public Document getDocument() {
    return null;
  }
}
