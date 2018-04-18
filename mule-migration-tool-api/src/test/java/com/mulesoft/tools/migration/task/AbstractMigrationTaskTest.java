/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.task;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mulesoft.tools.migration.exception.MigrationTaskException;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.step.category.ApplicationModelContribution;
import com.mulesoft.tools.migration.step.category.ExpressionContribution;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.NamespaceContribution;
import com.mulesoft.tools.migration.step.category.PomContribution;
import com.mulesoft.tools.migration.step.category.ProjectStructureContribution;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Mulesoft Inc.
 */
public class AbstractMigrationTaskTest {

  private MigrationTask migrationTask;
  private ApplicationModel applicationModelMock;

  @Before
  public void setUp() throws Exception {
    migrationTask = new MigrationTaskImpl();
    applicationModelMock = mock(ApplicationModel.class);
    when(applicationModelMock.getPomModel()).thenReturn(Optional.empty());
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
    migrationTask.execute(mock(MigrationReport.class));
  }

  @Test
  public void executeWithNullMigrationSteps() throws Exception {
    migrationTask.setApplicationModel(applicationModelMock);
    ((MigrationTaskImpl) migrationTask).setMigrationSteps(null);

    migrationTask.execute(mock(MigrationReport.class));
  }

  @Test
  public void executeWithPlainMigrationStep() throws Exception {
    MigrationStep stepMock = mock(MigrationStep.class);
    List<MigrationStep> steps = new ArrayList<>();
    steps.add(stepMock);

    migrationTask.setApplicationModel(applicationModelMock);
    ((MigrationTaskImpl) migrationTask).setMigrationSteps(steps);

    migrationTask.execute(mock(MigrationReport.class));

    verify(stepMock, times(0)).execute(eq(applicationModelMock), any(MigrationReport.class));
  }

  @Test
  public void execute() throws Exception {
    NamespaceContribution namespaceContributionMock = mock(NamespaceContribution.class);
    ApplicationModelContribution applicationModelContributionMock = mock(ApplicationModelContribution.class);
    ExpressionContribution expressionContributionMock = mock(ExpressionContribution.class);
    ProjectStructureContribution projectStructureContributionMock = mock(ProjectStructureContribution.class);
    PomContribution pomContributionMock = mock(PomContribution.class);
    PomModel pomModelMock = mock(PomModel.class);

    List<MigrationStep> steps = new ArrayList<>();
    steps.add(namespaceContributionMock);
    steps.add(applicationModelContributionMock);
    steps.add(expressionContributionMock);
    steps.add(projectStructureContributionMock);
    steps.add(pomContributionMock);

    InOrder inOrder = Mockito.inOrder(steps.toArray());

    migrationTask.setApplicationModel(applicationModelMock);
    ((MigrationTaskImpl) migrationTask).setMigrationSteps(new ArrayList<>(steps));

    migrationTask.execute(mock(MigrationReport.class));
    verify(namespaceContributionMock, times(1)).execute(any(ApplicationModel.class), any(MigrationReport.class));
    verify(applicationModelContributionMock, times(1)).getAppliedTo();
    verify(expressionContributionMock, times(1)).execute(any(Object.class), any(MigrationReport.class));
    verify(projectStructureContributionMock, times(1)).execute(any(Path.class), any(MigrationReport.class));
    verify(pomContributionMock, times(1)).execute(any(PomModel.class), any(MigrationReport.class));

    inOrder.verify(namespaceContributionMock).execute(any(ApplicationModel.class), any(MigrationReport.class));
    inOrder.verify(expressionContributionMock).execute(any(Object.class), any(MigrationReport.class));
    inOrder.verify(projectStructureContributionMock).execute(any(Path.class), any(MigrationReport.class));
    inOrder.verify(pomContributionMock).execute(any(PomModel.class), any(MigrationReport.class));

  }

  @Test(expected = MigrationTaskException.class)
  public void executeWithFailedMigrationStep() throws Exception {
    NamespaceContribution namespaceContribution = mock(NamespaceContribution.class);
    doThrow(NullPointerException.class)
        .when(namespaceContribution)
        .execute(eq(applicationModelMock), any(MigrationReport.class));
    List<MigrationStep> steps = new ArrayList<>();
    steps.add(namespaceContribution);

    migrationTask.setApplicationModel(applicationModelMock);
    ((MigrationTaskImpl) migrationTask).setMigrationSteps(steps);

    migrationTask.execute(mock(MigrationReport.class));
  }

  private static final class MigrationTaskImpl extends AbstractMigrationTask {

    private List<MigrationStep> migrationSteps;

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public List<MigrationStep> getSteps() {
      return this.migrationSteps;
    }

    public void setMigrationSteps(List<MigrationStep> migrationSteps) {
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
