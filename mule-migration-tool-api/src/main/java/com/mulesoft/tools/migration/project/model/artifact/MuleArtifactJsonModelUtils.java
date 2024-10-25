/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.artifact;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.version.VersionUtils.MIN_MULE4_VALID_VERSION;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mule.runtime.api.deployment.meta.Product.MULE_EE;

import com.google.common.io.Files;
import com.google.gson.JsonElement;
import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.api.deployment.meta.MuleArtifactLoaderDescriptor;
import org.mule.runtime.api.deployment.meta.MuleArtifactLoaderDescriptorBuilder;
import org.mule.runtime.api.deployment.persistence.MuleApplicationModelJsonSerializer;

import java.io.File;
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
    String muleApplicationModelJson = format("{ \"minMuleVersion\": \"%s\" }", minMuleVersion);
    return new MuleArtifactJsonModel(muleApplicationModelJson);
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
    String muleApplicationModelJson =
        format("{ \"minMuleVersion\": \"%s\", \"secureProperties\": %s }", minMuleVersion, secureProperties);
    return new MuleArtifactJsonModel(muleApplicationModelJson);
  }

  /**
   * Builds a mule-artifact.json representational model from the specified path.
   *
   * @return a {@link MuleArtifactJsonModel}
   */
  public static MuleArtifactJsonModel buildMuleArtifactJson(Path muleArtifactJson) throws IOException {
    File muleArtifactJsonFile = muleArtifactJson.toFile();
    if (muleArtifactJsonFile.exists()) {
      String muleApplicationModelJson = Files.toString(muleArtifactJsonFile, MULE_ARTIFACT_DEFAULT_CHARSET);
      return new MuleArtifactJsonModel(muleApplicationModelJson);
    } else {
      return buildMinimalMuleArtifactJson(MIN_MULE4_VALID_VERSION);
    }
  }
}
