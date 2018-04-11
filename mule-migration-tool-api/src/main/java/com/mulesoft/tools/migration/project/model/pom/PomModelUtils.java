/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.model.pom;

/**
 * Some helper functions to manage the pom model.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class PomModelUtils {

  public static final String MULE_APPLICATION_4_PACKAGING_TYPE = "mule-application";
  public static final String MULE_APPLICATION_3_PACKAGING_TYPE = "mule";
  public static final String DEFAULT_MODEL_VERSION = "4.0.0";
  public static final String MULE_MAVEN_PLUGIN_ARTIFACT_ID = "mule-maven-plugin";
  public static final String MULE_MAVEN_PLUGIN_GROUP_ID = "org.mule.tools.maven";
  public static final String MULE_MAVEN_PLUGIN_VERSION = "3.0.0";
  private static final String MULESOFT_RELEASES = "mulesoft-releases";
  private static final String MULESOFT_RELEASES_REPOSITORY_NAME = "MuleSoft Releases Repository";
  private static final String MULESOFT_RELEASES_REPOSITORY_URL = "https://repository.mulesoft.org/releases/";
  private static final String DEFAULT_LAYOUT = "default";

  public static PomModel buildMinimalMule4ApplicationPom(String groupId, String artifactId, String version) {
    PomModel model = new PomModel();
    model.setGroupId(groupId);
    model.setArtifactId(artifactId);
    model.setVersion(version);
    model.setPackaging(MULE_APPLICATION_4_PACKAGING_TYPE);
    model.setModelVersion(DEFAULT_MODEL_VERSION);

    Plugin muleMavenPlugin = buildMule4MuleMavenPluginConfiguration();
    model.addPlugin(muleMavenPlugin);

    model.addRepository(getMuleReleasesRepository());

    Repository pluginsRepository = getMuleReleasesRepository();
    pluginsRepository.setSnapshotsEnabled(true);
    model.addPluginRepository(pluginsRepository);

    return model;
  }

  public static Plugin buildMule4MuleMavenPluginConfiguration() {
    return new Plugin.PluginBuilder()
        .withArtifactId(MULE_MAVEN_PLUGIN_ARTIFACT_ID)
        .withGroupId(MULE_MAVEN_PLUGIN_GROUP_ID)
        .withVersion(MULE_MAVEN_PLUGIN_VERSION).build();
  }

  public static Repository getMuleReleasesRepository() {
    Repository repository = new Repository.RepositoryBuilder().withId(MULESOFT_RELEASES).build();
    repository.setName(MULESOFT_RELEASES_REPOSITORY_NAME);
    repository.setUrl(MULESOFT_RELEASES_REPOSITORY_URL);
    repository.setLayout(DEFAULT_LAYOUT);
    return repository;
  }
}
