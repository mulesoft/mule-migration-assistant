/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.task;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mulesoft.tools.migration.exception.MigrationTaskException;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.step.category.ApplicationModelContribution;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.NamespaceContribution;
import com.mulesoft.tools.migration.step.category.PomContribution;
import com.mulesoft.tools.migration.step.category.ProjectStructureContribution;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;
import org.jdom2.xpath.XPathExpression;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Mulesoft Inc.
 */
public class AbstractMigrationTaskTest {

  private AbstractMigrationTask migrationTask;
  private ApplicationModel applicationModelMock;
  private ExpressionMigrator expressionMigratorMock;

  @Before
  public void setUp() throws Exception {
    migrationTask = new MigrationTaskImpl();
    applicationModelMock = mock(ApplicationModel.class);
    expressionMigratorMock = mock(ExpressionMigrator.class);
    when(applicationModelMock.getPomModel()).thenReturn(Optional.empty());
    migrationTask.setExpressionMigrator(expressionMigratorMock);

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
    ProjectStructureContribution projectStructureContributionMock = mock(ProjectStructureContribution.class);
    PomContribution pomContributionMock = mock(PomContribution.class);

    List<MigrationStep> steps = new ArrayList<>();
    steps.add(namespaceContributionMock);
    steps.add(applicationModelContributionMock);
    steps.add(projectStructureContributionMock);
    steps.add(pomContributionMock);

    InOrder inOrder = Mockito.inOrder(steps.toArray());

    migrationTask.setApplicationModel(applicationModelMock);
    ((MigrationTaskImpl) migrationTask).setMigrationSteps(new ArrayList<>(steps));

    migrationTask.execute(mock(MigrationReport.class));
    verify(namespaceContributionMock, times(1)).execute(any(ApplicationModel.class), any(MigrationReport.class));
    verify(applicationModelContributionMock, times(2)).getAppliedTo();
    verify(projectStructureContributionMock, times(1)).execute(isNull(), any(MigrationReport.class));
    verify(pomContributionMock, times(1)).execute(any(PomModel.class), any(MigrationReport.class));

    inOrder.verify(namespaceContributionMock).execute(any(ApplicationModel.class), any(MigrationReport.class));
    inOrder.verify(projectStructureContributionMock).execute(isNull(), any(MigrationReport.class));
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

  @Test
  public void shouldExecuteAllStepsAppModelContributionAndElementPresent() {
    doReturn(newArrayList(mock(Element.class))).when(applicationModelMock).getNodes(any(XPathExpression.class));

    MigrationStepSelector selectorMock = mock(MigrationStepSelector.class);
    ApplicationModelContribution contrib = mock(ApplicationModelContribution.class);
    doReturn(mock(XPathExpression.class)).when(contrib).getAppliedTo();
    doReturn(newArrayList(contrib)).when(selectorMock).getApplicationModelContributionSteps();

    AbstractMigrationTask taskSpy = spy(AbstractMigrationTask.class);
    taskSpy.setApplicationModel(applicationModelMock);

    assertThat("It should return true", taskSpy.shouldExecuteAllSteps(selectorMock));
  }

  @Test
  public void shouldExecuteAllStepsAppModelContributionAndElementNotPresent() {
    doReturn(newArrayList()).when(applicationModelMock).getNodes(anyString());

    MigrationStepSelector selectorMock = mock(MigrationStepSelector.class);
    doReturn(newArrayList(mock(ApplicationModelContribution.class))).when(selectorMock).getApplicationModelContributionSteps();

    AbstractMigrationTask taskSpy = spy(AbstractMigrationTask.class);
    taskSpy.setApplicationModel(applicationModelMock);

    assertThat("It should return false", !taskSpy.shouldExecuteAllSteps(selectorMock));
  }

  @Test
  public void shouldExecuteAllStepsNoAppModelContributionAndElementPresent() {
    doReturn(newArrayList(mock(Element.class))).when(applicationModelMock).getNodes(anyString());

    MigrationStepSelector selectorMock = mock(MigrationStepSelector.class);
    doReturn(newArrayList()).when(selectorMock).getApplicationModelContributionSteps();

    AbstractMigrationTask taskSpy = spy(AbstractMigrationTask.class);
    taskSpy.setApplicationModel(applicationModelMock);

    assertThat("It should return true", taskSpy.shouldExecuteAllSteps(selectorMock));
  }

  @Test
  public void shouldExecuteAllStepsNoAppModelContributionAndNoElementPresent() {
    doReturn(newArrayList()).when(applicationModelMock).getNodes(anyString());

    MigrationStepSelector selectorMock = mock(MigrationStepSelector.class);
    doReturn(newArrayList()).when(selectorMock).getApplicationModelContributionSteps();

    AbstractMigrationTask taskSpy = spy(AbstractMigrationTask.class);
    taskSpy.setApplicationModel(applicationModelMock);

    assertThat("It should return true", taskSpy.shouldExecuteAllSteps(selectorMock));
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
    public String getTo() {
      return null;
    }

    @Override
    public String getFrom() {
      return null;
    }

    @Override
    protected boolean shouldExecuteAllSteps(MigrationStepSelector stepSelector) {
      return true;
    }
  }

}
