/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.model.artifact;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mule.runtime.api.deployment.meta.Product.MULE_EE;

import com.google.common.io.Files;
import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.api.deployment.meta.MuleArtifactLoaderDescriptor;
import org.mule.runtime.api.deployment.meta.MuleArtifactLoaderDescriptorBuilder;
import org.mule.runtime.api.deployment.persistence.MuleApplicationModelJsonSerializer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * Some helper functions to manage the mule artifact json model.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MuleArtifactJsonModelUtils {

  public static final Charset MULE_ARTIFACT_DEFAULT_CHARSET = UTF_8;
  private static final String MULE_ID = "mule";
  protected static MuleApplicationModelJsonSerializer serializer = new MuleApplicationModelJsonSerializer();

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
    MuleApplicationModel muleApplicationModel = serializer.deserialize(format("{ \"minMuleVersion\": \"%s\" }", minMuleVersion));
    return new MuleArtifactJsonModel(muleApplicationModel);
  }

  /**
   * Builds a minimal mule-artifact.json representational model with the specified name.
   *
   * @param minMuleVersion
   * @return a {@link MuleArtifactJsonModel}
   */
  public static MuleArtifactJsonModel buildMinimalMuleArtifactJson(String minMuleVersion, List<String> secureProperties) {
    if (secureProperties == null || secureProperties.isEmpty()) {
      return buildMinimalMuleArtifactJson(minMuleVersion);
    }
    secureProperties = secureProperties.stream().map(prop -> "\"" + prop + "\"").collect(toList());
    MuleApplicationModel muleApplicationModel = serializer
        .deserialize(format("{ \"minMuleVersion\": \"%s\", \"secureProperties\": %s }", minMuleVersion, secureProperties));
    return new MuleArtifactJsonModel(muleApplicationModel);
  }

  /**
   * Builds a mule-artifact.json representational model from the specified path.
   *
   * @return a {@link MuleArtifactJsonModel}
   */
  public static MuleArtifactJsonModel buildMuleArtifactJson(Path muleArtifactJson) throws IOException {
    MuleApplicationModel muleApplicationModel =
        serializer.deserialize(format(Files.toString(muleArtifactJson.toFile(), MULE_ARTIFACT_DEFAULT_CHARSET)));
    return new MuleArtifactJsonModel(muleApplicationModel);
  }

}
