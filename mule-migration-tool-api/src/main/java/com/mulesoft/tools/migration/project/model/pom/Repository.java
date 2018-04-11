/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.model.pom;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.RepositoryPolicy;

import static com.google.common.base.Preconditions.checkArgument;


/**
 * Repository that can be added to both repositories and plugin repositories
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Repository {

  private final org.apache.maven.model.Repository repository;

  private Repository(org.apache.maven.model.Repository repository) {
    this.repository = repository;
  }

  private void setId(String id) {
    repository.setId(id);
  }

  public String getId() {
    return repository.getId();
  }

  public void setName(String name) {
    repository.setName(name);
  }

  public String getName() {
    return repository.getName();
  }

  public void setUrl(String url) {
    repository.setUrl(url);
  }

  public String getUrl() {
    return repository.getUrl();
  }

  public void setLayout(String layout) {
    repository.setLayout(layout);
  }

  public String getLayout() {
    return repository.getLayout();
  }

  public void setSnapshotsEnabled(boolean enabled) {
    RepositoryPolicy snapshotEnabled = new RepositoryPolicy();
    snapshotEnabled.setEnabled(enabled);
    repository.setSnapshots(snapshotEnabled);
  }

  public boolean areSnapshotsEnabled() {
    return repository.getSnapshots().isEnabled();
  }

  public org.apache.maven.model.Repository getInnerModel() {
    return repository;
  }

  /**
   * The repository builder.
   *
   * @author Mulesoft Inc.
   * @since 1.0.0
   */
  public static class RepositoryBuilder {

    private String id;

    public RepositoryBuilder withId(String id) {
      this.id = id;
      return this;
    }

    public Repository build() {
      checkArgument(StringUtils.isNotBlank(id), "Id cannot be null nor blank");
      Repository repository = new Repository(new org.apache.maven.model.Repository());
      repository.setId(id);

      return repository;
    }
  }
}
