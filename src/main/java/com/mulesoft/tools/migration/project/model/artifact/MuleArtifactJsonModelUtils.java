/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.model.artifact;

import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.api.deployment.meta.MuleArtifactLoaderDescriptor;
import org.mule.runtime.api.deployment.meta.MuleArtifactLoaderDescriptorBuilder;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.mule.runtime.api.deployment.meta.Product.MULE_EE;

/**
 * Some helper functions to manage the mule artifact json model.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MuleArtifactJsonModelUtils {

  private static final String MIN_MULE_VERSION = "4.1.1";
  private static final String EXPORTED_RESOURCES = "exportedResources";
  private static final String MULE_ID = "mule";

  /**
   * Builds a minimal mule-artifact.json representational model with the specified name.
   *
   * @param name the name to be set in the mule artifact model
   * @return a mule artifact json model with the minimum required information
   */
  public static MuleArtifactJsonModel buildMinimalMule4ArtifactJson(String name) {
    MuleApplicationModel.MuleApplicationModelBuilder builder = new MuleApplicationModel.MuleApplicationModelBuilder();

    builder.setName(name);
    builder.setSecureProperties(newArrayList());
    builder.setRedeploymentEnabled(true);
    builder.setMinMuleVersion(MIN_MULE_VERSION);
    builder.setRequiredProduct(MULE_EE);

    MuleArtifactLoaderDescriptor descriptor =
        new MuleArtifactLoaderDescriptorBuilder().setId(MULE_ID).addProperty(EXPORTED_RESOURCES, newArrayList()).build();
    builder.withClassLoaderModelDescriptorLoader(descriptor);

    MuleArtifactLoaderDescriptor loaderDescriptor =
        new MuleArtifactLoaderDescriptorBuilder().setId(MULE_ID).build();
    builder.withBundleDescriptorLoader(loaderDescriptor);

    return new MuleArtifactJsonModel(builder.build());
  }
}
