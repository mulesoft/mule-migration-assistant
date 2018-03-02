/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.job;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.mulesoft.tools.migration.configuration.ConfigurationParser;
import com.mulesoft.tools.migration.engine.MigrationJob.MigrationJobBuilder;
import com.mulesoft.tools.migration.project.structure.mule.three.MuleApplicationProject;

@Ignore
public class MigrationJobMunitTest {

  private static final String ORIGINAL_PROJECT_NAME = "original-project";
  private static final String MIGRATED_PROJECT_NAME = "migrated-project";
  private static final String MUNIT_TASKS_PATH = "munit/tasks";
  private static final String MUNIT_SECTIONS_SAMPLE_XML = "munit-sections-sample.xml";
  private static final Path MUNIT_EXAMPLES_PATH = Paths.get("munit/examples");
  private static final Path MUNIT_SECTIONS_SAMPLE_PATH = MUNIT_EXAMPLES_PATH.resolve(MUNIT_SECTIONS_SAMPLE_XML);


  private MigrationJobBuilder migrationJobBuilder;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private Path originalProjectPath;
  private Path migratedProjectPath;

  @Before
  public void setUp() throws Exception {

    buildOriginalProject();
    migratedProjectPath = temporaryFolder.newFolder(MIGRATED_PROJECT_NAME).toPath();


    migrationJobBuilder = new MigrationJobBuilder()
        .withProject(new MuleApplicationProject(originalProjectPath))
        .withOutputProject(new MuleApplicationProject(migratedProjectPath));


  }

  private void buildOriginalProject() throws IOException {
    originalProjectPath = temporaryFolder.newFolder(ORIGINAL_PROJECT_NAME).toPath();

    File app = originalProjectPath.resolve("src").resolve("main").resolve("app").toFile();
    app.mkdirs();

    URL sample = this.getClass().getClassLoader().getResource(MUNIT_SECTIONS_SAMPLE_PATH.toString());
    FileUtils.copyURLToFile(sample, new File(app, MUNIT_SECTIONS_SAMPLE_PATH.getFileName().toString()));
  }

  @Test
  public void executeMockTask() throws Exception {

    Path configurationPath = Paths.get(this.getClass().getClassLoader().getResource(MUNIT_TASKS_PATH.toString()).getPath());
    migrationJobBuilder.withMigrationTasks(new ConfigurationParser(configurationPath).parse());
    migrationJobBuilder.build().execute();
  }


  //  TODO this tests need to be updated and moved to a different place
  //
  //   @Ignore
  //   @Test
  //   public void testExecuteEmptySteps() throws Exception {
  //   migrationJobBuilder.execute();
  //   assertNotNull(migrationJobBuilder);
  //   }
  //
  //   @Ignore
  //   @Test
  //   public void checkStepExecution() throws Exception {
  //
  //   MigrationTask task = new MigrationTask("//munit:test");
  //   MigrationStep step;
  //
  //   step = new AddAttribute("description", "MyNewDescription2");
  //
  //   task.addStep(step);
  //
  //   // migrationJobBuilder.addTask(task);
  //   migrationJobBuilder.execute();
  //   }
  //
  //   @Test
  //   public void checkMultipleStepExecution() throws Exception {
  //
  //   MigrationTask task = new MigrationTask("//munit:test");
  //   MigrationStep step;
  //
  //   step = new AddAttribute("description", "MyNewDescription4");
  //
  //   task.addStep(step);
  //
  //   step = new AddAttribute("enable", "false");
  //
  //   task.addStep(step);
  //
  //   migrationJobBuilder.addTask(task);
  //   migrationJobBuilder.execute();
  //
  //   List<Element> nodesModified = getElementsFromDocument(migrationJobBuilder.getDocument(), "//munit:test");
  //   assertEquals(34, nodesModified.size());
  //   }
  //
  //   @Test
  //   public void checkMoveSetMessagePayloadExecution() throws Exception {
  //
  //   migrationJobBuilder = new MigrationJob();
  //   migrationJobBuilder.setDocuments(new ArrayList<String>(Arrays.asList(EXAMPLE_2_FILE_PATH)));
  //
  //   SetTasksForSetMessageNodesMigration();
  //
  //   migrationJobBuilder.execute();
  //
  //   List<Element> nodesModified = getElementsFromDocument(migrationJobBuilder.getDocument(), "//munit:test/munit:set-event");
  //   assertEquals(4, nodesModified.size());
  //   }
  //
  //
  //   @Test
  //   public void changeAssertDSL() throws Exception {
  //   migrationJobBuilder = new MigrationJob();
  //   migrationJobBuilder.setDocuments(new ArrayList<String>(Arrays.asList(EXAMPLE_2_FILE_PATH)));
  //
  //   SetTasksForAssertsNodesMigration();
  //
  //   migrationJobBuilder.execute();
  //
  //   List<Element> nodesModified = getElementsFromDocument(migrationJobBuilder.getDocument(), "//munit:test/assert:that");
  //   assertEquals(14, nodesModified.size());
  //   }
  //
  //   @Test
  //   public void executeMultipleTasks() throws Exception {
  //   migrationJobBuilder = new MigrationJob();
  //   migrationJobBuilder.setDocuments(new ArrayList<String>(Arrays.asList(EXAMPLE_2_FILE_PATH)));
  //
  //   SetTasksForAssertsNodesMigration();
  //   SetTasksForSetMessageNodesMigration();
  //
  //   migrationJobBuilder.execute();
  //
  //   List<Element> nodesModified = getElementsFromDocument(migrationJobBuilder.getDocument(), "//munit:test/assert:that");
  //   assertEquals(14, nodesModified.size());
  //   }
  //
  //   @Ignore
  //   @Test
  //   public void migrateMultipleFiles() throws Exception {
  //
  //   ArrayList<String> files = new ArrayList<String>(asList(EXAMPLE_2_FILE_PATH, EXAMPLE_1_FILE_PATH));
  //
  //   setTasksForAssertsNodesMigration();
  //   setTasksForSetMessageNodesMigration();
  //
  //   // migrationJobBuilder.setDocuments(files);
  //   migrationJobBuilder.execute();
  //
  //   }
  //
  //   @Test
  //   public void jobWithTasksOnConfigFile() throws Exception {
  //   List<String> files = asList(EXAMPLE_2_FILE_PATH, EXAMPLE_1_FILE_PATH);
  //
  //   migrationJobBuilder.setDocuments(files);
  //   new MuleApplication(Paths.get(projectBasePath));
  //   migrationJobBuilder.setMigrationTasks(new ConfigurationParser(Paths.get(MUNIT_TASKS_PATH)).parse());
  //
  //   migrationJobBuilder.execute();
  //   }
  //
  //
  //
  //
  //   @Test
  //   public void executeCompleteMUnitMigrationTasks() throws Exception {
  //   List<String> files = asList(MUNIT_SECTIONS_SAMPLE_PATH);
  //
  //   migrationJobBuilder.setDocuments(files);
  //   migrationJobBuilder.setMigrationTasks(new ConfigurationParser(Paths.get(MUNIT_TASKS_PATH)).parse());
  //   migrationJobBuilder.setOnErrorStop(false);
  //   migrationJobBuilder.execute();
  //   }
  //
  //   @After
  //   public void restoreFileState() throws Exception {
  //   DocumentHelper.restoreTestDocument(docRestoreFile1, EXAMPLE_1_FILE_PATH);
  //   DocumentHelper.restoreTestDocument(docRestoreFile2, EXAMPLE_2_FILE_PATH);
  //   DocumentHelper.restoreTestDocument(docRestoreFile3, MUNIT_SECTIONS_SAMPLE_PATH);
  //   DocumentHelper.restoreTestDocument(docRestoreFile4, EXAMPLE_4_FILE_PATH);
  //   }

  //  private void setTasksForAssertsNodesMigration() {
  //
  //    MigrationTask assertTask;
  //    MigrationStep step;
  //
  //    List<MigrationTask> migrationTasks = new ArrayList<>();
  //
  //    assertTask =
  //        new MigrationTask("//munit:test/*[contains(local-name(),'assert') or contains(local-name(),'fail') or contains(local-name(),'run-custom')]");
  //    step = new SetNodeNamespace("assert", "http://www.mulesoft.org/schema/mule/assert",
  //                                "http://www.mulesoft.org/schema/mule/assert/current/mule-assert.xsd");
  //    assertTask.addStep(step);
  //
  //    step = new ReplaceStringOnNodeName("assert-", "");
  //    assertTask.addStep(step);
  //
  //    migrationTasks.add(assertTask);
  //
  //    assertTask = new MigrationTask("//munit:test/*[contains(local-name(),'payload-equals')]");
  //    step = new ReplaceNodesName("assert", "that");
  //    assertTask.addStep(step);
  //    step = new AddAttribute("expression", "#[payload]");
  //    assertTask.addStep(step);
  //    step = new UpdateAttributeName("expectedValue", "is");
  //    assertTask.addStep(step);
  //
  //    migrationTasks.add(assertTask);
  //
  //    assertTask = new MigrationTask("//munit:test/*[contains(local-name(),'not-null')]");
  //    step = new ReplaceNodesName("assert", "that");
  //    assertTask.addStep(step);
  //    step = new AddAttribute("expression", "#[payload]");
  //    assertTask.addStep(step);
  //    step = new AddAttribute("is", "#[not(nullValue())]");
  //    assertTask.addStep(step);
  //
  //    migrationTasks.add(assertTask);
  //
  //    assertTask = new MigrationTask("//munit:test/*[contains(local-name(),'null')]");
  //    step = new ReplaceNodesName("assert", "that");
  //    assertTask.addStep(step);
  //    step = new AddAttribute("expression", "#[payload]");
  //    assertTask.addStep(step);
  //    step = new AddAttribute("is", "#[nullValue()]");
  //    assertTask.addStep(step);
  //
  //    migrationTasks.add(assertTask);
  //
  //    assertTask = new MigrationTask("//munit:test/*[contains(local-name(),'false')]");
  //    step = new ReplaceNodesName("assert", "that");
  //    assertTask.addStep(step);
  //    step = new UpdateAttributeName("condition", "expression");
  //    assertTask.addStep(step);
  //    step = new AddAttribute("is", "#[false]");
  //    assertTask.addStep(step);
  //
  //    migrationTasks.add(assertTask);
  //
  //    assertTask = new MigrationTask("//munit:test/*[contains(local-name(),'true')]");
  //    step = new ReplaceNodesName("assert", "that");
  //    assertTask.addStep(step);
  //    step = new UpdateAttributeName("condition", "expression");
  //    assertTask.addStep(step);
  //    step = new AddAttribute("is", "#[true]");
  //    assertTask.addStep(step);
  //
  //    migrationTasks.add(assertTask);
  //
  //    assertTask = new MigrationTask("//munit:test/*[contains(local-name(),'on-equals')]");
  //    step = new ReplaceNodesName("assert", "that");
  //    assertTask.addStep(step);
  //    step = new UpdateAttributeName("expectedValue", "is");
  //    assertTask.addStep(step);
  //    step = new UpdateAttributeName("actualValue", "expression");
  //    assertTask.addStep(step);
  //
  //    migrationTasks.add(assertTask);
  //
  //    assertTask = new MigrationTask("//munit:test/*[contains(local-name(),'not-same')]");
  //    step = new ReplaceNodesName("assert", "that");
  //    assertTask.addStep(step);
  //    step = new UpdateAttributeName("expectedValue", "is");
  //    assertTask.addStep(step);
  //    step = new UpdateAttributeName("actualValue", "expression");
  //    assertTask.addStep(step);
  //    step = new NegateAttributeValue("is");
  //    assertTask.addStep(step);
  //
  //    migrationTasks.add(assertTask);
  //
  //    migrationJobBuilder.withMigrationTasks(migrationTasks);
  //  }

  //  private void setTasksForSetMessageNodesMigration() {
  //
  //    List<MigrationTask> migrationTasks = new ArrayList<>();
  //
  //
  //    MigrationTask task = new MigrationTask("//munit:test/munit:set");
  //    MigrationStep step;
  //
  //    step = new ReplaceNodesName("munit", "set-event");
  //    task.addStep(step);
  //    step = new CreateChildNodeFromAttribute("payload");
  //    task.addStep(step);
  //    step = new MoveAttributeToChildNode("encoding", "payload");
  //    task.addStep(step);
  //    step = new MoveAttributeToChildNode("mimeType", "payload");
  //    task.addStep(step);
  //
  //    migrationTasks.add(task);
  //
  //    task = new MigrationTask("/descendant::*[attribute::mimeType]");
  //    step = new UpdateAttributeName("mimeType", "mediaType");
  //    task.addStep(step);
  //
  //    migrationTasks.add(task);
  //
  //    task = new MigrationTask("//munit:test/munit:set-event/munit:invocation-properties");
  //    step = new ReplaceNodesName("munit", "variables");
  //    task.addStep(step);
  //
  //    migrationTasks.add(task);
  //
  //    task = new MigrationTask("//munit:test/munit:set-event/munit:variables/munit:invocation-property");
  //    step = new ReplaceNodesName("munit", "variable");
  //    task.addStep(step);
  //
  //    migrationTasks.add(task);
  //
  //    migrationJobBuilder.withMigrationTasks(migrationTasks);
  //  }
}
