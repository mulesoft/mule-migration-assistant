/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.model.artifact;

import static com.google.common.base.Preconditions.checkArgument;
import static com.mulesoft.tools.migration.project.model.artifact.MuleArtifactJsonModelUtils.buildMinimalMuleArtifactJson;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.api.deployment.meta.MuleArtifactLoaderDescriptor;
import org.mule.runtime.api.deployment.meta.Product;

import static com.mulesoft.tools.migration.util.version.VersionUtils.MIN_MULE4_VALID_VERSION;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.mule.runtime.api.deployment.meta.Product.valueOf;

import org.mule.runtime.api.deployment.persistence.MuleApplicationModelJsonSerializer;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The mule-artifact.json representational model.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MuleArtifactJsonModel {

  public static final String BUNDLE_DESCRIPTOR_LOADER = "bundleDescriptorLoader";
  public static final String ID = "id";
  public static final String ATTRIBUTES = "attributes";
  public static final String CLASS_LOADER_MODEL_LOADER_DESCRIPTOR = "classLoaderModelLoaderDescriptor";
  private Gson gson = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization().create();
  private final JsonObject model;
  public static final String NAME = "name";
  public static final String CONFIGS = "configs";
  public static final String MIN_MULE_VERSION_FIELD = "minMuleVersion";
  public static final String REDEPLOYMENT_ENABLED = "redeploymentEnabled";
  public static final String SECURE_PROPERTIES = "secureProperties";
  public static final String REQUIRED_PRODUCT = "requiredProduct";

  /**
   * Create a mule artifact representation
   *
   * @param muleArtifactJsonContent the string content of the mule-artifact.json
   */
  public MuleArtifactJsonModel(String muleArtifactJsonContent) {
    JsonParser parser = new JsonParser();
    model = (JsonObject) parser.parse(muleArtifactJsonContent);
  }

  /**
   * Create a mule artifact representation
   *
   * @param model the mule application model to represent on the mule artifact file
   */
  public MuleArtifactJsonModel(MuleApplicationModel model) {
    this(new MuleApplicationModelJsonSerializer().serialize(model));
  }

  /**
   * Add a new property to the model
   *
   * @param key the property key name
   * @param value the property key value
   * @throws IllegalArgumentException
   */
  public void addProperty(String key, String value) throws IllegalArgumentException {
    checkArgument(key != null, "Key should not be null");
    checkArgument(value != null, "Value should not be null");
    model.addProperty(key, value);
  }

  /**
   * Retrieve a key from the model
   *
   * @param key the key to retrieve
   * @return an optional {@link Object}
   */
  public Optional<Object> getProperty(String key) {
    if (has(key)) {
      return ofNullable(model.get(key).getAsString());
    }
    return empty();
  }

  /**
   * Check that the json object contains a member with the specified name
   *
   * @param member the name of the representation to check
   * @return {@link Boolean}
   */
  public boolean has(String member) {
    checkArgument(member != null, "Member should not be null");
    return model.has(member);
  }

  /**
   * Remove a member representation on the json object
   *
   * @param member the name of the representation to remove
   * @return {@link Boolean}
   */
  public boolean remove(String member) {
    checkArgument(member != null, "Member should not be null");
    return model.remove(member) != null;
  }

  /**
   * Get the minimum mule version where the application will run from
   *
   * @return {@link String} with the minimum mule version
   */
  public String getMinMuleVersion() {
    if (model.has(MIN_MULE_VERSION_FIELD)) {
      return model.get(MIN_MULE_VERSION_FIELD).getAsString();
    }
    model.addProperty(MIN_MULE_VERSION_FIELD, MIN_MULE4_VALID_VERSION);
    return MIN_MULE4_VALID_VERSION;
  }

  /**
   * Set the minimum mule version where the application will run from
   *
   * @param minMuleVersion the minimum version to set on the json
   */
  public void setMinMuleVersion(String minMuleVersion) {
    checkArgument(minMuleVersion != null, "minMuleVersion should not be null");
    model.addProperty(MIN_MULE_VERSION_FIELD, minMuleVersion);
  }

  /**
   * Get the application secure properties defined on the mule artifact file
   *
   * @return an optional of {@link List<String>} with the secure properties
   */
  public Optional<List<String>> getSecureProperties() {
    if (model.has(SECURE_PROPERTIES)) {
      return ofNullable(gson.fromJson(model.getAsJsonArray(SECURE_PROPERTIES), new TypeToken<List<String>>() {}.getType()));
    }
    return empty();
  }

  /**
   * Set the application secure properties
   *
   * @param secureProperties a {@link List<String>} of all the properties to define on the mule artifact file
   */
  public void setSecureProperties(List<String> secureProperties) {
    checkArgument(secureProperties != null, "secureProperties should not be null");
    JsonElement secureProps = gson.toJsonTree(secureProperties, new TypeToken<List<String>>() {}.getType());
    model.add(SECURE_PROPERTIES, secureProps);
  }

  /**
   * Get all the mule configuration files defined on the mule artifact file
   *
   * @return an optional of {@link Set<String>} with the name of the config files
   */
  public Optional<Set<String>> getConfigs() {
    if (model.has(CONFIGS)) {
      return ofNullable(gson.fromJson(model.getAsJsonArray(CONFIGS), new TypeToken<Set<String>>() {}.getType()));
    }
    return empty();
  }

  /**
   * Set the mule configuration files on the mule artifact file
   *
   * @param configs a  {@link Set<String>} with the names of the config files
   */
  public void setConfigs(Set<String> configs) {
    checkArgument(configs != null, "configs should not be null");
    JsonElement configsList = gson.toJsonTree(configs, new TypeToken<Set<String>>() {}.getType());
    model.add(CONFIGS, configsList);
  }

  /**
   * Get the Bundle descriptor section on the mule artifact file
   *
   * @return an optional of {@link MuleArtifactLoaderDescriptor}
   */
  public Optional<MuleArtifactLoaderDescriptor> getBundleDescriptorLoader() {
    MuleArtifactLoaderDescriptor descriptor = null;
    String id = null;
    Map<String, Object> attributes = null;
    if (model.has(BUNDLE_DESCRIPTOR_LOADER)) {
      JsonObject bundleDescriptorLoader = (JsonObject) model.get(BUNDLE_DESCRIPTOR_LOADER);
      if (bundleDescriptorLoader.has(ID)) {
        id = bundleDescriptorLoader.get(ID).getAsString();
      }
      if (bundleDescriptorLoader.has(ATTRIBUTES)) {
        attributes = gson.fromJson(bundleDescriptorLoader.get(ATTRIBUTES), new TypeToken<Map<String, Object>>() {}.getType());
      }
      descriptor = new MuleArtifactLoaderDescriptor(id, attributes);
    }
    return Optional.ofNullable(descriptor);
  }

  /**
   * Set the Bundle descriptor section on the mule artifact file
   *
   * @param id the id of the Bundle Descriptor
   * @param attributes the map of attributes to define on the Bundle
   */
  public void setBundleDescriptorLoader(String id, Map<String, Object> attributes) {
    checkArgument(id != null, "id should not be null");
    checkArgument(attributes != null, "attributes should not be null");
    JsonElement descriptor = gson.toJsonTree(new MuleArtifactLoaderDescriptor(id, attributes),
                                             new TypeToken<MuleArtifactLoaderDescriptor>() {}.getType());
    model.add(BUNDLE_DESCRIPTOR_LOADER, descriptor);
  }

  /**
   * Get the Bundle descriptor section on the mule artifact file
   *
   * @return an optional of {@link MuleArtifactLoaderDescriptor}
   */
  public Optional<MuleArtifactLoaderDescriptor> getClassLoaderModelLoaderDescriptor() {
    MuleArtifactLoaderDescriptor descriptor = null;
    String id = null;
    Map<String, Object> attributes = null;
    if (model.has(CLASS_LOADER_MODEL_LOADER_DESCRIPTOR)) {
      JsonObject bundleDescriptorLoader = (JsonObject) model.get(CLASS_LOADER_MODEL_LOADER_DESCRIPTOR);
      if (bundleDescriptorLoader.has(ID)) {
        id = bundleDescriptorLoader.get(ID).getAsString();
      }
      if (bundleDescriptorLoader.has(ATTRIBUTES)) {
        attributes = gson.fromJson(bundleDescriptorLoader.get(ATTRIBUTES), new TypeToken<Map<String, Object>>() {}.getType());
      }
      descriptor = new MuleArtifactLoaderDescriptor(id, attributes);
    }
    return Optional.ofNullable(descriptor);
  }

  /**
   * Set the Classloader model section on the mule artifact file
   *
   * @param id the id of the classloader model
   * @param attributes the map of attributes to define on classloader model
   */
  public void setClassLoaderModelLoaderDescriptor(String id, Map<String, Object> attributes) {
    checkArgument(id != null, "id should not be null");
    checkArgument(attributes != null, "attributes should not be null");
    JsonElement descriptor = gson.toJsonTree(new MuleArtifactLoaderDescriptor(id, attributes),
                                             new TypeToken<MuleArtifactLoaderDescriptor>() {}.getType());
    model.add(CLASS_LOADER_MODEL_LOADER_DESCRIPTOR, descriptor);
  }

  /**
   * Get the application name defined on the mule artifact file
   *
   * @return an optional String with the application name
   */
  public Optional<String> getName() {
    if (model.has(NAME)) {
      return ofNullable(model.get(NAME).getAsString());
    }
    return empty();
  }

  /**
   * Set the application name on the mule artifact file
   *
   * @param name the name of the application
   */
  public void setName(String name) {
    checkArgument(name != null, "name should not be null");
    model.addProperty(NAME, name);
  }

  /**
   * Get the runtime product that the application requires (CE or EE)
   *
   * @return an optional of {@link Product}
   */
  public Optional<Product> getRequiredProduct() {
    if (model.has(REQUIRED_PRODUCT)) {
      return of(valueOf(model.get(REQUIRED_PRODUCT).getAsString()));
    }
    return empty();
  }

  /**
   * Set the runtime product that the application requires (CE or EE)
   *
   * @param requiredProduct the {@link Product} that the application requires
   */
  public void setRequiredProduct(Product requiredProduct) {
    checkArgument(requiredProduct != null, "requiredProduct should not be null");
    model.addProperty(REQUIRED_PRODUCT, requiredProduct.toString());
  }

  /**
   * Get the redeployment property defined on the mule artifact file
   *
   * @return an optional Boolean
   */
  public Optional<Boolean> getIsRedeploymentEnabled() {
    if (model.has(REDEPLOYMENT_ENABLED)) {
      return of(model.get(REDEPLOYMENT_ENABLED).getAsBoolean());
    }
    return empty();
  }

  /**
   * Set the redeployment property on the mule artifact file
   *
   * @param redeploymentEnabled {@link Boolean}
   */
  public void setIsRedeploymentEnabled(boolean redeploymentEnabled) {
    model.addProperty(REDEPLOYMENT_ENABLED, redeploymentEnabled);
  }

  /**
   * Get the model representation
   *
   * @return the model representation as a String
   */
  @Override
  public String toString() {
    return gson.toJson(model);
  }

  /**
   * The mule artifact model builder. It builds the mule artifact model based on the mule-artifact.json location in the filesystem.
   *
   * @author Mulesoft Inc.
   * @since 1.0.0
   */
  public static class MuleApplicationJsonModelBuilder {

    private Path muleArtifactJsonPath;
    private String muleVersion;

    public MuleApplicationJsonModelBuilder withMuleArtifactJson(Path muleArtifactJsonPath) {
      this.muleArtifactJsonPath = muleArtifactJsonPath;
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
        return buildMinimalMuleArtifactJson(muleVersion);
      }
      String muleArtifactJsonContent = FileUtils.readFileToString(muleArtifactJsonPath.toFile(), (String) null);

      return new MuleArtifactJsonModel(muleArtifactJsonContent);
    }
  }
}
