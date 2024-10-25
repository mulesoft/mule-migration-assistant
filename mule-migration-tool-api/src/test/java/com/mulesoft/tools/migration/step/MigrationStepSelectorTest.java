/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.step;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.ApplicationModelContribution;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.NamespaceContribution;
import com.mulesoft.tools.migration.step.category.PomContribution;
import com.mulesoft.tools.migration.step.category.ProjectStructureContribution;
import com.mulesoft.tools.migration.task.MigrationStepSelector;

import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mulesoft Inc.
 */
public class MigrationStepSelectorTest {

  private MigrationStepSelector migrationStepSelector;

  @Before
  public void setUp() throws Exception {
    migrationStepSelector = new MigrationStepSelector(buildStepSet());
  }

  @Test(expected = IllegalArgumentException.class)
  public void constructionWithNull() {
    new MigrationStepSelector(null);
  }

  @Test
  public void getNamespaceContributionSteps() {
    List<com.mulesoft.tools.migration.step.category.NamespaceContribution> steps =
        migrationStepSelector.getNameSpaceContributionSteps();
    assertThat("The step set size is wrong", steps.size(), is(1));
    assertThat("The step type is wrong", steps.iterator().next(), instanceOf(NamespaceContribution.class));
  }

  @Test
  public void getApplicationModelContributionSteps() {
    List<ApplicationModelContribution> steps = migrationStepSelector.getApplicationModelContributionSteps();
    assertThat("The step set size is wrong", steps.size(), is(1));
    assertThat("The step type is wrong", steps.iterator().next(), instanceOf(ApplicationModelContribution.class));
  }

  @Test
  public void getProjectStructureContributionSteps() {
    List<ProjectStructureContribution> steps = migrationStepSelector.getProjectStructureContributionSteps();
    assertThat("The step set size is wrong", steps.size(), is(1));
    assertThat("The step type is wrong", steps.iterator().next(), instanceOf(ProjectStructureContribution.class));
  }

  @Test
  public void getPomContributionSteps() {
    List<PomContribution> steps = migrationStepSelector.getPomContributionSteps();
    assertThat("The step set size is wrong", steps.size(), is(1));
    assertThat("The step type is wrong", steps.iterator().next(), instanceOf(PomContribution.class));
  }

  private List<MigrationStep> buildStepSet() {
    List<MigrationStep> migrationSteps = new ArrayList<>();

    migrationSteps.add(new NamespaceContributionStepImpl());
    migrationSteps.add(new ApplicationModelContributionStepImpl());
    migrationSteps.add(new ExpressionContributionStepImpl());
    migrationSteps.add(new ProjectStructureContributionStepImpl());
    migrationSteps.add(new PomContributionStepImpl());

    return migrationSteps;
  }


  private static final class NamespaceContributionStepImpl implements NamespaceContribution {

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public void execute(ApplicationModel object, MigrationReport report) throws RuntimeException {

    }
  }

  private static final class ApplicationModelContributionStepImpl extends AbstractApplicationModelMigrationStep {

    @Override
    public void execute(Element object, MigrationReport report) throws RuntimeException {

    }

    @Override
    public String getDescription() {
      return null;
    }

  }

  private static final class ExpressionContributionStepImpl implements MigrationStep {

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public void execute(Object object, MigrationReport report) throws RuntimeException {

    }
  }

  private static final class ProjectStructureContributionStepImpl implements ProjectStructureContribution {

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public void execute(Path object, MigrationReport report) throws RuntimeException {

    }
  }

  private static final class PomContributionStepImpl implements PomContribution {

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public void execute(PomModel object, MigrationReport report) throws RuntimeException {

    }
  }

}
