/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.model.artifact;

import static com.google.common.base.Preconditions.checkArgument;
import static com.mulesoft.tools.migration.project.model.artifact.MuleArtifactJsonModelUtils.buildMinimalMule4ArtifactJson;

import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.api.deployment.meta.MuleApplicationModel.MuleApplicationModelBuilder;
import org.mule.runtime.api.deployment.meta.MuleArtifactLoaderDescriptorBuilder;
import org.mule.runtime.api.deployment.persistence.MuleApplicationModelJsonSerializer;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * The mule-artifact.json representational model.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MuleArtifactJsonModel {

  private MuleApplicationModel model;
  private Set<String> exportedPackages = new TreeSet<>();

  protected MuleArtifactJsonModel(MuleApplicationModel muleApplicationModel) {
    this.model = muleApplicationModel;
  }

  @Override
  public String toString() {
    if (!exportedPackages.isEmpty()) {
      MuleArtifactLoaderDescriptorBuilder builder =
          new MuleArtifactLoaderDescriptorBuilder().setId(model.getClassLoaderModelLoaderDescriptor().getId());
      model.getClassLoaderModelLoaderDescriptor().getAttributes().forEach((k, v) -> {
        builder.addProperty(k, v);
      });

      MuleApplicationModelBuilder modelBuilder = new MuleApplicationModelBuilder();
      modelBuilder.setName(model.getName());
      modelBuilder.setRequiredProduct(model.getRequiredProduct());
      modelBuilder.setMinMuleVersion(model.getMinMuleVersion());
      modelBuilder.withBundleDescriptorLoader(model.getBundleDescriptorLoader());
      modelBuilder.withClassLoaderModelDescriptorLoader(builder.addProperty("exportedPackages", exportedPackages).build());
      modelBuilder.setConfigs(model.getConfigs());
      modelBuilder.setRedeploymentEnabled(model.isRedeploymentEnabled());
      modelBuilder.setSecureProperties(model.getSecureProperties());
      model = modelBuilder.build();
    }
    return new MuleApplicationModelJsonSerializer().serialize(model);

  }

  /**
   * The mule artifact model builder. It builds the mule artifact model based on the mule-artifact.json location in the filesystem.
   *
   * @author Mulesoft Inc.
   * @since 1.0.0
   */
  public static class MuleApplicationJsonModelBuilder {

    private final MuleApplicationModelJsonSerializer serializer = new MuleApplicationModelJsonSerializer();
    private Path muleArtifactJsonPath;
    private Collection<Path> configs;
    private String muleVersion;

    public MuleApplicationJsonModelBuilder withMuleArtifactJson(Path muleArtifactJsonPath) {
      this.muleArtifactJsonPath = muleArtifactJsonPath;
      return this;
    }

    public MuleApplicationJsonModelBuilder withConfigs(Collection<Path> configs) {
      this.configs = configs;
      return this;
    }

    public MuleApplicationJsonModelBuilder withMuleVersion(String muleVersion) {
      this.muleVersion = muleVersion;
      return this;
    }

    /**
     * Builds the mule-artifact based on the file pointed by the pom path. If such file does not exist, an empty model is returned.
     *
     * @return a mule-artifact.json model
     * @throws IOException
     */
    public MuleArtifactJsonModel build() throws IOException {
      checkArgument(muleArtifactJsonPath != null, "mule-artifact.json path should not be null");
      if (!muleArtifactJsonPath.toAbsolutePath().toFile().exists()
          && muleArtifactJsonPath.toAbsolutePath().getParent().toFile().exists()) {
        return buildMinimalMule4ArtifactJson(muleArtifactJsonPath.getParent().toFile().getName(), configs, muleVersion);
      }
      MuleApplicationModel model = getModel(muleArtifactJsonPath);
      return new MuleArtifactJsonModel(model);
    }


    private MuleApplicationModel getModel(Path muleArtifactJsonPath) throws IOException {
      String muleArtifactJsonContent = FileUtils.readFileToString(muleArtifactJsonPath.toFile(), (String) null);
      return serializer.deserialize(muleArtifactJsonContent);
    }

  }

  /**
   * Adds a package to be exported by the app.
   *
   * @param packageName the name of the package to be exported by the application so it's accessible from the runtime and plugins.
   */
  public void addExportedPackage(String packageName) {
    exportedPackages.add(packageName);
  }
}
