/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.model.artifact;

import org.apache.commons.io.FileUtils;
import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.api.deployment.persistence.MuleApplicationModelJsonSerializer;

import java.io.IOException;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkArgument;
import static com.mulesoft.tools.migration.project.model.artifact.MuleArtifactJsonModelUtils.buildMinimalMule4ArtifactJson;

/**
 * The mule-artifact.json representational model.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MuleArtifactJsonModel {

  private MuleApplicationModel model;

  protected MuleArtifactJsonModel(MuleApplicationModel muleApplicationModel) {
    this.model = muleApplicationModel;
  }

  public String toString() {
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

    public MuleApplicationJsonModelBuilder withMuleArtifactJson(Path muleArtifactJsonPath) {
      this.muleArtifactJsonPath = muleArtifactJsonPath;
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
        return buildMinimalMule4ArtifactJson(muleArtifactJsonPath.getParent().toFile().getName());
      }
      MuleApplicationModel model = getModel(muleArtifactJsonPath);
      return new MuleArtifactJsonModel(model);
    }


    private MuleApplicationModel getModel(Path muleArtifactJsonPath) throws IOException {
      String muleArtifactJsonContent = FileUtils.readFileToString(muleArtifactJsonPath.toFile(), (String) null);
      return serializer.deserialize(muleArtifactJsonContent);
    }

  }

}
