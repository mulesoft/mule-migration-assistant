/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.step;

import com.mulesoft.tools.migration.engine.step.category.*;
import com.mulesoft.tools.migration.pom.PomModel;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
    Set<NamespaceContribution> steps = migrationStepSorter.getNameSpaceContributionSteps();
    assertThat("The step set size is wrong", steps.size(), is(1));
    assertThat("The step type is wrong", steps.iterator().next(), instanceOf(NamespaceContribution.class));
  }

  @Test
  public void getApplicationModelContributionSteps() {
    Set<ApplicationModelContribution> steps = migrationStepSorter.getApplicationModelContributionSteps();
    assertThat("The step set size is wrong", steps.size(), is(1));
    assertThat("The step type is wrong", steps.iterator().next(), instanceOf(ApplicationModelContribution.class));
  }

  @Test
  public void getExpressionContributionSteps() {
    Set<ExpressionContribution> steps = migrationStepSorter.getExpressionContributionSteps();
    assertThat("The step set size is wrong", steps.size(), is(1));
    assertThat("The step type is wrong", steps.iterator().next(), instanceOf(ExpressionContribution.class));
  }

  @Test
  public void getProjectStructureContributionSteps() {
    Set<ProjectStructureContribution> steps = migrationStepSorter.getProjectStructureContributionSteps();
    assertThat("The step set size is wrong", steps.size(), is(1));
    assertThat("The step type is wrong", steps.iterator().next(), instanceOf(ProjectStructureContribution.class));
  }

  @Test
  public void getPomContributionSteps() {
    Set<PomContribution> steps = migrationStepSorter.getPomContributionSteps();
    assertThat("The step set size is wrong", steps.size(), is(1));
    assertThat("The step type is wrong", steps.iterator().next(), instanceOf(PomContribution.class));
  }

  private Set<MigrationStep> buildStepSet() {
    Set<MigrationStep> migrationSteps = new HashSet<>();

    migrationSteps.add(new NamespaceContributionStepImpl());
    migrationSteps.add(new ApplicationModelContributionStepImpl());
    migrationSteps.add(new ExpressionContributionStepImpl());
    migrationSteps.add(new ProjectStructureContributionStepImpl());
    migrationSteps.add(new PomContributionStepImpl());

    return migrationSteps;
  }


  private static final class NamespaceContributionStepImpl implements NamespaceContribution {

    @Override
    public String getAppliedTo() {
      return null;
    }

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public Element getElement() {
      return null;
    }

    @Override
    public void setElement(Element element) {

    }

    @Override
    public void setAppliedTo(String xpathExpression) {

    }

    @Override
    public void execute() throws Exception {

    }

    @Override
    public void setApplicationModel(ApplicationModel applicationModel) {

    }
  }

  private static final class ApplicationModelContributionStepImpl implements ApplicationModelContribution {

    @Override
    public String getAppliedTo() {
      return null;
    }

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public Element getElement() {
      return null;
    }

    @Override
    public void setElement(Element element) {

    }

    @Override
    public void setAppliedTo(String xpathExpression) {

    }

    @Override
    public void execute() throws Exception {

    }

  }

  private static final class ExpressionContributionStepImpl implements ExpressionContribution {

    @Override
    public void execute() throws Exception {

    }

    @Override
    public String getAppliedTo() {
      return null;
    }

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public Element getElement() {
      return null;
    }

    @Override
    public void setElement(Element element) {

    }

    @Override
    public void setAppliedTo(String xpathExpression) {

    }
  }

  private static final class ProjectStructureContributionStepImpl implements ProjectStructureContribution {

    @Override
    public String getAppliedTo() {
      return null;
    }

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public Element getElement() {
      return null;
    }

    @Override
    public void setElement(Element element) {

    }

    @Override
    public void setAppliedTo(String xpathExpression) {

    }

    @Override
    public void execute() throws Exception {

    }
  }

  private static final class PomContributionStepImpl implements PomContribution {

    @Override
    public String getAppliedTo() {
      return null;
    }

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public Element getElement() {
      return null;
    }

    @Override
    public void setElement(Element element) {

    }

    @Override
    public void setAppliedTo(String xpathExpression) {

    }

    @Override
    public void execute() throws Exception {

    }

    @Override
    public void setPomModel(PomModel pomModel) {

    }
  }


}
