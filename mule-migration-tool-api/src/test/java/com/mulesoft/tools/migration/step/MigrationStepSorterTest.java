/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.step;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.*;
import com.mulesoft.tools.migration.task.MigrationStepSorter;

import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mulesoft Inc.
 */
public class MigrationStepSorterTest {

  private MigrationStepSorter migrationStepSorter;

  @Before
  public void setUp() throws Exception {
    migrationStepSorter = new MigrationStepSorter(buildStepSet());
  }

  @Test(expected = IllegalArgumentException.class)
  public void constructionWithNull() {
    new MigrationStepSorter(null);
  }

  @Test
  public void getNamespaceContributionSteps() {
    List<com.mulesoft.tools.migration.step.category.NamespaceContribution> steps =
        migrationStepSorter.getNameSpaceContributionSteps();
    assertThat("The step set size is wrong", steps.size(), is(1));
    assertThat("The step type is wrong", steps.iterator().next(), instanceOf(NamespaceContribution.class));
  }

  @Test
  public void getApplicationModelContributionSteps() {
    List<ApplicationModelContribution> steps = migrationStepSorter.getApplicationModelContributionSteps();
    assertThat("The step set size is wrong", steps.size(), is(1));
    assertThat("The step type is wrong", steps.iterator().next(), instanceOf(ApplicationModelContribution.class));
  }

  @Test
  public void getExpressionContributionSteps() {
    List<ExpressionContribution> steps = migrationStepSorter.getExpressionContributionSteps();
    assertThat("The step set size is wrong", steps.size(), is(1));
    assertThat("The step type is wrong", steps.iterator().next(), instanceOf(ExpressionContribution.class));
  }

  @Test
  public void getProjectStructureContributionSteps() {
    List<ProjectStructureContribution> steps = migrationStepSorter.getProjectStructureContributionSteps();
    assertThat("The step set size is wrong", steps.size(), is(1));
    assertThat("The step type is wrong", steps.iterator().next(), instanceOf(ProjectStructureContribution.class));
  }

  @Test
  public void getPomContributionSteps() {
    List<PomContribution> steps = migrationStepSorter.getPomContributionSteps();
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

  private static final class ExpressionContributionStepImpl implements ExpressionContribution {

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
