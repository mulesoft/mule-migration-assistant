/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
