/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.pom;

import static com.google.common.base.Preconditions.checkArgument;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.RepositoryPolicy;


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

  /**
   * Sets the repository unique identifier.
   *
   * @param id
   */
  private void setId(String id) {
    repository.setId(id);
  }

  /**
   * Retrieves the repository unique identifier.
   *
   * @return {@link String}
   */
  public String getId() {
    return repository.getId();
  }

  /**
   * Sets the human readable name of the repository.
   *
   * @param name
   */
  public void setName(String name) {
    repository.setName(name);
  }

  /**
   * Retrieves the human readable name of the repository.
   *
   * @return {@link String}
   */
  public String getName() {
    return repository.getName();
  }

  /**
   * Sets the url of the repository in the protocol://hostname/path format.
   *
   * @param url
   */
  public void setUrl(String url) {
    repository.setUrl(url);
  }

  /**
   * Retrieves the url of the repository in the protocol://hostname/path format.
   *
   * @return {@link String}
   */
  public String getUrl() {
    return repository.getUrl();
  }

  /**
   * Sets the repository layout. Can be default or legacy.
   *
   * @param layout
   */
  public void setLayout(String layout) {
    repository.setLayout(layout);
  }

  /**
   * Retrieves the repository layout. It should be either default or legacy.
   *
   * @return {@link String}
   */
  public String getLayout() {
    return repository.getLayout();
  }

  /**
   * Sets whether to use this repository for downloading snapshot artifacts.
   *
   * @param enabled
   */
  public void setSnapshotsEnabled(boolean enabled) {
    RepositoryPolicy snapshotEnabled = new RepositoryPolicy();
    snapshotEnabled.setEnabled(enabled);
    repository.setSnapshots(snapshotEnabled);
  }

  /**
   * Retrieves whether this repository is set to download snapshot artifacts.
   *
   * @return a boolean
   */
  public boolean areSnapshotsEnabled() {
    return repository.getSnapshots().isEnabled();
  }

  public org.apache.maven.model.Repository getInnerModel() {
    return repository;
  }

  static final Repository of(org.apache.maven.model.Repository repo) {
    return new Repository(repo);
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
