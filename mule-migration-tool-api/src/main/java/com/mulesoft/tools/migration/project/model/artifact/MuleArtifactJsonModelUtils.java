/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.model.artifact;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.mule.runtime.api.deployment.meta.Product.MULE_EE;

import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.api.deployment.meta.MuleArtifactLoaderDescriptor;
import org.mule.runtime.api.deployment.meta.MuleArtifactLoaderDescriptorBuilder;
import org.mule.runtime.api.deployment.persistence.MuleApplicationModelJsonSerializer;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Some helper functions to manage the mule artifact json model.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MuleArtifactJsonModelUtils {

  private static final String MULE_ID = "mule";
  private static MuleApplicationModelJsonSerializer serializer = new MuleApplicationModelJsonSerializer();

  /**
   * Builds a minimal mule-artifact.json representational model with the specified name.
   *
   * @param name the name to be set in the mule artifact model
   * @return a {@link MuleArtifactJsonModel}
   */
  public static MuleArtifactJsonModel buildMule4ArtifactJson(String name, Collection<Path> configs, String muleVersion) {
    MuleApplicationModel.MuleApplicationModelBuilder builder = new MuleApplicationModel.MuleApplicationModelBuilder();

    builder.setName(name);
    builder.setSecureProperties(newArrayList());
    builder.setRedeploymentEnabled(true);
    builder.setMinMuleVersion(muleVersion);
    builder.setRequiredProduct(MULE_EE);

    if (configs != null && !configs.isEmpty()) {
      Set<String> configsNames = new HashSet<>();
      configs.forEach(c -> configsNames.add(c.getFileName().toString()));
      builder.setConfigs(configsNames);
    } else {
      builder.setConfigs(null);
    }

    MuleArtifactLoaderDescriptor descriptor =
        new MuleArtifactLoaderDescriptorBuilder().setId(MULE_ID).build();
    builder.withClassLoaderModelDescriptorLoader(descriptor);

    MuleArtifactLoaderDescriptor loaderDescriptor =
        new MuleArtifactLoaderDescriptorBuilder().setId(MULE_ID).build();
    builder.withBundleDescriptorLoader(loaderDescriptor);

    return new MuleArtifactJsonModel(builder.build());
  }

  /**
   * Builds a minimal mule-artifact.json representational model with the specified name.
   *
   * @param minMuleVersion
   * @return a {@link MuleArtifactJsonModel}
   */
  public static MuleArtifactJsonModel buildMinimalMuleArtifactJson(String minMuleVersion) {
    MuleApplicationModel muleApplicationModel = serializer.deserialize(format("{ minMuleVersion: %s }", minMuleVersion));
    return new MuleArtifactJsonModel(muleApplicationModel);
  }
}
