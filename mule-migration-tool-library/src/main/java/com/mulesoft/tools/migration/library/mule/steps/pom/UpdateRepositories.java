/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.pom;

import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.project.model.pom.Repository;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Updates mulesoft repositories definitions.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class UpdateRepositories implements PomContribution {

  @Override
  public String getDescription() {
    return "Updates mulesoft repositories definitions";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport report) {
    for (Repository repository : pomModel.getRepositories()) {
      if (repository.getUrl().startsWith("http://repository.mulesoft.org")) {
        repository.setUrl(repository.getUrl().replace("http://repository.mulesoft.org", "https://repository.mulesoft.org"));
      }
    }

    final Repository exchangeRepo = new Repository.RepositoryBuilder().withId("anypoint-exchange").build();
    exchangeRepo.setName("Anypoint Exchange");
    exchangeRepo.setUrl("https://maven.anypoint.mulesoft.com/api/v1/maven");
    exchangeRepo.setLayout("default");
    pomModel.addRepository(exchangeRepo);

    for (Repository repository : pomModel.getPluginRepositories()) {
      if (repository.getUrl().startsWith("http://repository.mulesoft.org")) {
        repository.setUrl(repository.getUrl().replace("http://repository.mulesoft.org", "https://repository.mulesoft.org"));
      }
    }
  }
}
