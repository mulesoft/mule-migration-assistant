/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.pom;

import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.project.model.pom.Repository;
import com.mulesoft.tools.migration.tck.ReportVerification;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;

public class UpdateRepositoriesTest {

  private static final String POM_WITH_REPOSITORIES = "/pommodel/pomWithRepositories/pom.xml";

  @Rule
  public ReportVerification report = new ReportVerification();

  private PomModel model;
  private UpdateRepositories updateRpositories;

  @Before
  public void setUp() {
    updateRpositories = new UpdateRepositories();
  }

  @Test
  public void exchangeRepoAdded() throws Exception {
    Path pomPath = Paths.get(getClass().getResource(POM_WITH_REPOSITORIES).toURI());
    model = new PomModel.PomModelBuilder().withPom(pomPath).build();

    updateRpositories.execute(model, report.getReport());

    final Predicate<Repository> excangeRepoMatcher =
        r -> "anypoint-exchange".equals(r.getId()) && "https://maven.anypoint.mulesoft.com/api/v1/maven".equals(r.getUrl());
    model.getRepositories().stream().anyMatch(excangeRepoMatcher);
    model.getPluginRepositories().stream().noneMatch(excangeRepoMatcher);
  }

  @Test
  public void muleReposSchemeUpdated() throws Exception {
    Path pomPath = Paths.get(getClass().getResource(POM_WITH_REPOSITORIES).toURI());
    model = new PomModel.PomModelBuilder().withPom(pomPath).build();

    updateRpositories.execute(model, report.getReport());

    final Predicate<Repository> httpRepoMatcher = r -> r.getUrl().startsWith("http://repository.mulesoft.org");
    final Predicate<Repository> httpsRepoMatcher = r -> r.getUrl().startsWith("http://repository.mulesoft.org");
    model.getRepositories().stream().noneMatch(httpRepoMatcher);
    model.getRepositories().stream().anyMatch(httpsRepoMatcher);
    model.getPluginRepositories().stream().noneMatch(httpRepoMatcher);
    model.getPluginRepositories().stream().anyMatch(httpsRepoMatcher);

  }

  @Test
  public void customerReposKept() throws Exception {
    Path pomPath = Paths.get(getClass().getResource(POM_WITH_REPOSITORIES).toURI());
    model = new PomModel.PomModelBuilder().withPom(pomPath).build();

    updateRpositories.execute(model, report.getReport());

    final Predicate<Repository> ownRepoMatcher =
        r -> "own".equals(r.getId()) && "customers own repo".equals(r.getName()) && "http://my.org/maven2/".equals(r.getUrl());
    model.getRepositories().stream().anyMatch(ownRepoMatcher);
    model.getPluginRepositories().stream().anyMatch(ownRepoMatcher);

  }
}
