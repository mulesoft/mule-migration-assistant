/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.artifact;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mule.runtime.api.deployment.meta.MuleArtifactLoaderDescriptor;
import org.mule.runtime.api.deployment.meta.Product;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.mulesoft.tools.migration.project.model.artifact.MuleArtifactJsonModel.BUNDLE_DESCRIPTOR_LOADER;
import static com.mulesoft.tools.migration.project.model.artifact.MuleArtifactJsonModel.CLASS_LOADER_MODEL_LOADER_DESCRIPTOR;
import static com.mulesoft.tools.migration.project.model.artifact.MuleArtifactJsonModel.CONFIGS;
import static com.mulesoft.tools.migration.project.model.artifact.MuleArtifactJsonModel.NAME;
import static com.mulesoft.tools.migration.project.model.artifact.MuleArtifactJsonModel.REDEPLOYMENT_ENABLED;
import static com.mulesoft.tools.migration.project.model.artifact.MuleArtifactJsonModel.REQUIRED_PRODUCT;
import static com.mulesoft.tools.migration.util.version.VersionUtils.MIN_MULE4_VALID_VERSION;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;


public class MuleArtifactJsonModelTest {

  public static final String MIN_MULE_VERSION = "minMuleVersion";
  public static final String SECURE_PROPERTIES = "secureProperties";
  private static final String MULE_ARTIFACT_FILE = "mule-artifact.json";
  private static final String MULE_VERSION = "4.1.1";
  public static final String EMPTY_MULE_ARTIFACT_JSON_CONTENT = "{}";
  public static final String RANDOM_VERSION = "4.1.1";
  private MuleArtifactJsonModel model;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private MuleArtifactJsonModel.MuleApplicationJsonModelBuilder builder;

  @Before
  public void setUp() {
    builder = new MuleArtifactJsonModel.MuleApplicationJsonModelBuilder();
    model = new MuleArtifactJsonModel(EMPTY_MULE_ARTIFACT_JSON_CONTENT);
  }

  @Test(expected = IllegalArgumentException.class)
  public void buildNullPath() throws IOException {
    model = builder.withMuleArtifactJson(null).build();
  }

  @Test
  public void buildWithNonExistentMuleArtifact() throws IOException {
    model = builder.withMuleArtifactJson(temporaryFolder.getRoot().toPath().resolve(MULE_ARTIFACT_FILE))
        .withMuleVersion(MULE_VERSION)
        .build();
    assertThat("Name expecting minimal mule artifact",
               model.toString().matches("^\\{\\s*\"minMuleVersion\":\\s*\"" + MULE_VERSION + "\"\\s*}$"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void addNullKeyProperty() {
    model.addProperty(null, "value");
  }

  @Test(expected = IllegalArgumentException.class)
  public void addNullValueProperty() {
    model.addProperty("key", null);
  }

  @Test
  public void addProperty() {
    String key = "key", value = "value";
    assertThat("Key should not be present", !model.has(key));
    model.addProperty(key, value);
    assertThat("Key should be present and mapping to value", model.getProperty(key).get(), equalTo(value));
  }

  @Test
  public void addSameProperty() {
    String key = "key", value = "value", otherValue = "otherValue";
    assertThat("Key should not be present", !model.getProperty(key).isPresent());
    model.addProperty(key, value);
    model.addProperty(key, otherValue);
    assertThat("Key should be present and mapping to otherValue", model.getProperty(key).get(), equalTo(otherValue));
  }

  @Test
  public void getProperty() {
    String key = "key", value = "value";
    model = new MuleArtifactJsonModel(format("{ \"%s\": \"%s\" }", key, value));
    assertThat("Model should contain key", model.has(key));
    assertThat("Key should be mapping to value", model.getProperty(key).get(), equalTo(value));
  }

  @Test
  public void has() {
    String key = "key", value = "value";
    assertThat("Key should not be present", !model.has(key));
    model.addProperty(key, value);
    assertThat("Key should be present", model.has(key));
  }

  @Test(expected = IllegalArgumentException.class)
  public void hasNull() {
    model.has(null);
  }

  @Test
  public void remove() {
    String key = "key", value = "value";
    model = new MuleArtifactJsonModel(format("{ \"%s\": \"%s\" }", key, value));
    assertThat("Model should contain key", model.has(key));
    assertThat("Key should be removed", model.remove(key));
    assertThat("Model should not contain key", !model.has(key));
  }

  @Test
  public void removeNonExistent() {
    assertThat("Key should not be removed if it does not exist", !model.remove("key"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void removeNull() {
    model.remove(null);
  }

  @Test
  public void getMinMuleVersion() {
    model = new MuleArtifactJsonModel(format("{ \"%s\": \"%s\" }", MIN_MULE_VERSION, RANDOM_VERSION));
    assertThat("minMuleVersion is not the expected", model.getMinMuleVersion(), equalTo(RANDOM_VERSION));
  }

  @Test
  public void getMinMuleVersionEmpty() {
    assertThat("minMuleVersion should not be defined already", !model.has(MIN_MULE_VERSION));
    assertThat("minMuleVersion should be the default", model.getMinMuleVersion(), equalTo(MIN_MULE4_VALID_VERSION));
  }

  @Test
  public void setMinMuleVersion() {
    model = new MuleArtifactJsonModel(format("{ \"%s\": \"%s\" }", MIN_MULE_VERSION, RANDOM_VERSION));
    assertThat("minMuleVersion is not the expected", model.getMinMuleVersion(), equalTo(RANDOM_VERSION));

    String newVersion = "4.4.4";
    model.setMinMuleVersion(newVersion);
    assertThat("minMuleVersion is not the expected", model.getMinMuleVersion(), equalTo(newVersion));
  }

  @Test(expected = IllegalArgumentException.class)
  public void setMinMuleVersionNull() {
    model.setMinMuleVersion(null);
  }

  @Test
  public void getSecureProperties() {
    List<String> securePropertiesList = newArrayList("lala", "pepe");
    model = new MuleArtifactJsonModel(format("{ \"%s\": %s }", SECURE_PROPERTIES, securePropertiesList));
    assertThat("secureProperties should be present", model.has(SECURE_PROPERTIES));
    List<String> securePropertiesListActual = model.getSecureProperties().get();
    assertThat("secureProperties is not the expected", securePropertiesListActual, equalTo(securePropertiesList));
  }

  @Test
  public void setSecureProperties() {
    List<String> securePropertiesList = newArrayList("lala", "pepe");
    assertThat("secureProperties should be present", !model.has(SECURE_PROPERTIES));
    model.setSecureProperties(securePropertiesList);
    assertThat("secureProperties are not present in the JSON content", model.toString(),
               containsString("\"secureProperties\": [\n    \"lala\",\n    \"pepe\"\n  ]"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void setSecurePropertiesNull() {
    model.setSecureProperties(null);
  }

  @Test
  public void getConfigs() {
    Set<String> configsSet = newHashSet("config1.xml", "config2.xml");
    model = new MuleArtifactJsonModel(format("{ \"%s\": %s }", CONFIGS, configsSet));
    assertThat("configs should be present", model.has(CONFIGS));
    Set<String> configsSetActual = model.getConfigs().get();
    assertThat("configs is not the expected", configsSetActual, equalTo(configsSet));
  }

  @Test(expected = IllegalArgumentException.class)
  public void setConfigsNull() {
    model.setConfigs(null);
  }

  @Test
  public void setConfigs() {
    Set<String> configsSet = newHashSet("config1.xml", "config2.xml");
    assertThat("configs should be present", !model.has(CONFIGS));
    model.setConfigs(configsSet);
    assertThat("configs are not present in the JSON content",
               model.toString().contains("\"configs\": [\n    \"config2.xml\",\n    \"config1.xml\"\n  ]"));
  }

  @Test
  public void getBundleDescriptorLoader() {
    assertThat("bundleDescriptorLoader should not be present", !model.getBundleDescriptorLoader().isPresent());
    model = new MuleArtifactJsonModel(format("{ \"%s\": %s }", BUNDLE_DESCRIPTOR_LOADER,
                                             "{\"id\": \"mule\", \"attributes\": { \"groupId\": \"group.id\"} }"));
    String id = "mule", key = "groupId", value = "group.id";
    Map<String, Object> attributes = new HashMap<>();
    attributes.put(key, value);

    MuleArtifactLoaderDescriptor descriptor = model.getBundleDescriptorLoader().get();
    assertThat("Id is not the expected", descriptor.getId(), equalTo(id));
    assertThat("Attributes map is not the expected", descriptor.getAttributes(), equalTo(attributes));
  }

  @Test
  public void setBundleDescriptorLoader() {
    assertThat("bundleDescriptorLoader should not be present", !model.getBundleDescriptorLoader().isPresent());

    String id = "id", key = "key", value = "value";
    Map<String, Object> attributes = new HashMap<>();
    attributes.put(key, value);

    model.setBundleDescriptorLoader(id, attributes);

    MuleArtifactLoaderDescriptor descriptor = model.getBundleDescriptorLoader().get();
    assertThat("Id is not the expected", descriptor.getId(), equalTo(id));
    assertThat("Attributes map is not the expected", descriptor.getAttributes(), equalTo(attributes));
  }

  @Test(expected = IllegalArgumentException.class)
  public void setBundleDescriptorLoaderNullId() {
    model.setBundleDescriptorLoader(null, new HashMap<>());
  }

  @Test(expected = IllegalArgumentException.class)
  public void setBundleDescriptorLoaderNullAttributes() {
    model.setBundleDescriptorLoader("mule", null);
  }

  @Test
  public void getClassLoaderModelLoaderDescriptor() {
    assertThat("classLoaderModelLoaderDescriptor should not be present",
               !model.getClassLoaderModelLoaderDescriptor().isPresent());
    model = new MuleArtifactJsonModel(format("{ \"%s\": %s }", CLASS_LOADER_MODEL_LOADER_DESCRIPTOR,
                                             "{\"id\": \"mule\", \"attributes\": { \"groupId\": \"group.id\"} }"));
    String id = "mule", key = "groupId", value = "group.id";
    Map<String, Object> attributes = new HashMap<>();
    attributes.put(key, value);

    MuleArtifactLoaderDescriptor descriptor = model.getClassLoaderModelLoaderDescriptor().get();
    assertThat("Id is not the expected", descriptor.getId(), equalTo(id));
    assertThat("Attributes map is not the expected", descriptor.getAttributes(), equalTo(attributes));
  }

  @Test
  public void setClassLoaderModelLoaderDescriptor() {
    assertThat("classLoaderModelLoaderDescriptor should not be present",
               !model.getClassLoaderModelLoaderDescriptor().isPresent());

    String id = "id", key = "key", value = "value";
    Map<String, Object> attributes = new HashMap<>();
    attributes.put(key, value);

    model.setClassLoaderModelLoaderDescriptor(id, attributes);

    MuleArtifactLoaderDescriptor descriptor = model.getClassLoaderModelLoaderDescriptor().get();
    assertThat("Id is not the expected", descriptor.getId(), equalTo(id));
    assertThat("Attributes map is not the expected", descriptor.getAttributes(), equalTo(attributes));
  }

  @Test(expected = IllegalArgumentException.class)
  public void setClassLoaderModelLoaderDescriptorNullId() {
    model.setClassLoaderModelLoaderDescriptor(null, new HashMap<>());
  }

  @Test(expected = IllegalArgumentException.class)
  public void setClassLoaderModelLoaderDescriptorNullAttributes() {
    model.setClassLoaderModelLoaderDescriptor("mule", null);
  }

  @Test
  public void getName() {
    assertThat("name should not be present", !model.getName().isPresent());
    String name = "lala";
    model = new MuleArtifactJsonModel(format("{ \"%s\": %s }", NAME, name));
    assertThat("name should be present", model.getName().isPresent());
    assertThat("name is not the expected", model.getName().get(), equalTo(name));
  }

  @Test
  public void setName() {
    assertThat("name should not be present", !model.getName().isPresent());
    String name = "lala";
    model.setName(name);
    assertThat("name should be present", model.getName().isPresent());
    assertThat("name is not the expected", model.getName().get(), equalTo(name));
  }

  @Test(expected = IllegalArgumentException.class)
  public void setNameNull() {
    model.setName(null);
  }

  @Test
  public void getRequiredProduct() {
    assertThat("requiredProduct should not be present", !model.getRequiredProduct().isPresent());
    Product product = Product.MULE_EE;
    model = new MuleArtifactJsonModel(format("{ \"%s\": %s }", REQUIRED_PRODUCT, product));
    assertThat("requiredProduct should be present", model.getRequiredProduct().isPresent());
    assertThat("requiredProduct is not the expected", model.getRequiredProduct().get(), equalTo(product));
  }

  @Test
  public void setRequiredProduct() {
    assertThat("requiredProduct should not be present", !model.getRequiredProduct().isPresent());
    Product product = Product.MULE_EE;
    model.setRequiredProduct(product);
    assertThat("requiredProduct should be present", model.getRequiredProduct().isPresent());
    assertThat("requiredProduct is not the expected", model.getRequiredProduct().get(), equalTo(product));
    assertThat("JSON content is not the expected", model.toString().contains("\"requiredProduct\": \"MULE_EE\""));
  }

  @Test(expected = IllegalArgumentException.class)
  public void setRequiredProductNull() {
    model.setRequiredProduct(null);
  }

  @Test
  public void isRedeploymentEnabled() {
    assertThat("redeploymentEnabled should not be present", !model.getIsRedeploymentEnabled().isPresent());
    model = new MuleArtifactJsonModel(format("{ \"%s\": %s }", REDEPLOYMENT_ENABLED, true));
    assertThat("redeploymentEnabled should be present", model.getIsRedeploymentEnabled().isPresent());
    assertThat("redeploymentEnabled should be true", model.getIsRedeploymentEnabled().get());
  }

  @Test
  public void setIsRedeploymentEnabled() {
    model = new MuleArtifactJsonModel(format("{ \"%s\": %s }", REDEPLOYMENT_ENABLED, true));
    assertThat("redeploymentEnabled should be true", model.getIsRedeploymentEnabled().get());
    model.setIsRedeploymentEnabled(false);
    assertThat("redeploymentEnabled should be false", !model.getIsRedeploymentEnabled().get());
  }
}
