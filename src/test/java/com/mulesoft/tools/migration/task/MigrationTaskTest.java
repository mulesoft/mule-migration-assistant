/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.task;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import com.mulesoft.tools.migration.engine.step.MigrationStep;
import org.junit.Ignore;
import org.junit.Test;

import com.mulesoft.tools.migration.engine.step.DefaultMigrationStep;
import com.mulesoft.tools.migration.engine.task.DefaultMigrationTask;
import com.mulesoft.tools.migration.library.step.AddAttribute;
import com.mulesoft.tools.migration.project.structure.ProjectType;

@Ignore
public class MigrationTaskTest {

  private DefaultMigrationTask migrationTask;

  private static final String EXAMPLE_FILE_PATH = "src/test/resources/munit/examples/sample-file.xml";

  @Ignore
  @Test
  public void setNullSelector() throws Exception {
    migrationTask = new MigrationTaskTestImpl();
    migrationTask.execute();
    assertEquals(0, getListSize(migrationTask));
  }

  @Ignore
  @Test
  public void setEmptySelector() throws Exception {
    migrationTask = new MigrationTaskTestImpl();
    migrationTask.execute();
    assertEquals(0, getListSize(migrationTask));
  }

  @Ignore
  @Test
  public void setSelectorForNoNode() throws Exception {
    migrationTask = new MigrationTaskTestImpl();
    migrationTask.setDocument(getDocument(EXAMPLE_FILE_PATH));
    migrationTask.execute();
    assertEquals(0, getListSize(migrationTask));
  }

  @Ignore
  @Test
  public void addNullStepToTask() throws Exception {
    migrationTask = new MigrationTaskTestImpl();
    migrationTask.setDocument(getDocument(EXAMPLE_FILE_PATH));
    migrationTask.addStep(null);
    migrationTask.execute();
    assertEquals(0, getListSize(migrationTask));
  }

  @Ignore
  @Test
  public void setSelectorForNodes() throws Exception {
    migrationTask = new MigrationTaskTestImpl();
    migrationTask.setDocument(getDocument(EXAMPLE_FILE_PATH));
    migrationTask.execute();
    assertEquals(7, getListSize(migrationTask));
  }

  //  @Ignore
  //  @Test
  //  public void setSelectorForNodesAndExecuteStep() throws Exception {
  //    migrationTask = new MigrationTaskTestImpl();
  //    migrationTask.setDocument(getDocument(EXAMPLE_FILE_PATH));
  //    DefaultMigrationStep step = new AddAttribute("pepe", "pepa");
  //    migrationTask.addStep(step);
  //    migrationTask.execute();
  //    assertEquals(7, getListSize(migrationTask));
  //  }
  //
  //  @Ignore
  //  @Test
  //  public void setSelectorForNodesAndExecuteStepEmptyDoc() throws Exception {
  //    migrationTask = new MigrationTaskTestImpl();
  //    DefaultMigrationStep attStep = new AddAttribute("pepe", "test");
  //    migrationTask.addStep(attStep);
  //    migrationTask.execute();
  //    assertEquals(0, getListSize(migrationTask));
  //  }


  public int getListSize(DefaultMigrationTask task) throws Exception {
    int size;
    Field field = task.getClass().getDeclaredField("nodes");
    field.setAccessible(true);
    size = ((List) field.get(task)).size();
    return size;
  }

  public static class MigrationTaskTestImpl extends DefaultMigrationTask {

    @Override
    public String getTo() {
      return "";
    }

    @Override
    public String getFrom() {
      return "";
    }

    @Override
    public ProjectType getProjectType() {
      return null;
    }

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public Set<MigrationStep> getSteps() {
      return null;
    }
  }
}
