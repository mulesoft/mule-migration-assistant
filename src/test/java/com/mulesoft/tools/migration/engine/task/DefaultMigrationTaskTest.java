/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.task;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.InOrder.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mulesoft.tools.migration.engine.step.category.ApplicationModelContribution;
import com.mulesoft.tools.migration.engine.step.category.ExpressionContribution;
import com.mulesoft.tools.migration.engine.step.category.NamespaceContribution;
import com.mulesoft.tools.migration.engine.step.category.PomContribution;
import com.mulesoft.tools.migration.engine.step.category.ProjectStructureContribution;
import org.junit.Before;
import org.junit.Test;

import com.mulesoft.tools.migration.engine.step.MigrationStep;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.structure.ProjectType;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.internal.InOrderImpl;

/**
 * @author Mulesoft Inc.
 */
public class DefaultMigrationTaskTest {

  private MigrationTask migrationTask;
  private ApplicationModel applicationModelMock;

  @Before
  public void setUp() throws Exception {
    migrationTask = new MigrationTaskImpl();
    applicationModelMock = mock(ApplicationModel.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void setApplicationModelNull() {
    migrationTask.setApplicationModel(null);
  }

  @Test
  public void setApplicationModel() {
    migrationTask.setApplicationModel(applicationModelMock);
    assertThat("The application model is not as expected", migrationTask.getApplicationModel(), is(applicationModelMock));
  }

  @Test(expected = IllegalStateException.class)
  public void executeWithNullApplicationModel() throws Exception {
    migrationTask.execute();
  }

  @Test
  public void executeWithNullMigrationSteps() throws Exception {
    migrationTask.setApplicationModel(applicationModelMock);
    ((MigrationTaskImpl) migrationTask).setMigrationSteps(null);

    migrationTask.execute();
  }

  @Test
  public void executeWithPlainMigrationStep() throws Exception {
    MigrationStep stepMock = mock(MigrationStep.class);
    Set<MigrationStep> steps = new HashSet<>();
    steps.add(stepMock);

    migrationTask.setApplicationModel(applicationModelMock);
    ((MigrationTaskImpl) migrationTask).setMigrationSteps(steps);

    migrationTask.execute();

    verify(stepMock, times(0)).execute();
  }

  @Test
  public void execute() throws Exception {
    NamespaceContribution namespaceContributionMock = mock(NamespaceContribution.class);
    ApplicationModelContribution applicationModelContributionMock = mock(ApplicationModelContribution.class);
    ExpressionContribution expressionContributionMock = mock(ExpressionContribution.class);
    ProjectStructureContribution projectStructureContributionMock = mock(ProjectStructureContribution.class);
    PomContribution pomContributionMock = mock(PomContribution.class);

    List<MigrationStep> steps = new ArrayList<>();
    steps.add(namespaceContributionMock);
    steps.add(applicationModelContributionMock);
    steps.add(expressionContributionMock);
    steps.add(projectStructureContributionMock);
    steps.add(pomContributionMock);

    InOrder inOrder = Mockito.inOrder(steps.toArray());

    migrationTask.setApplicationModel(applicationModelMock);
    ((MigrationTaskImpl) migrationTask).setMigrationSteps(new HashSet<>(steps));

    migrationTask.execute();

    verify(namespaceContributionMock, times(1)).execute();
    verify(applicationModelContributionMock, times(1)).execute();
    verify(applicationModelContributionMock, times(1)).setApplicationModel(applicationModelMock);
    verify(expressionContributionMock, times(1)).execute();
    verify(projectStructureContributionMock, times(1)).execute();
    verify(pomContributionMock, times(1)).execute();

    inOrder.verify(namespaceContributionMock).execute();
    inOrder.verify(applicationModelContributionMock).execute();
    inOrder.verify(expressionContributionMock).execute();
    inOrder.verify(projectStructureContributionMock).execute();
    inOrder.verify(pomContributionMock).execute();

  }

  private static final class MigrationTaskImpl extends DefaultMigrationTask {

    private Set<MigrationStep> migrationSteps;

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public Set<MigrationStep> getSteps() {
      return this.migrationSteps;
    }

    public void setMigrationSteps(Set<MigrationStep> migrationSteps) {
      this.migrationSteps = migrationSteps;
    }


    @Override
    public Version getTo() {
      return null;
    }

    @Override
    public Version getFrom() {
      return null;
    }

    @Override
    public ProjectType getProjectType() {
      return null;
    }
  }

}
