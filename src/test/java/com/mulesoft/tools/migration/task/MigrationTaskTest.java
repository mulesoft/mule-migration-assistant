package com.mulesoft.tools.migration.task;

import com.mulesoft.tools.migration.task.steps.AddAttribute;
import com.mulesoft.tools.migration.task.steps.MigrationStep;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

import static com.mulesoft.tools.migration.helpers.DocumentHelpers.getDocument;
import static org.junit.Assert.*;

public class MigrationTaskTest {

    private MigrationTask migrationTask;

    private static final String EXAMPLE_FILE_PATH = "src/test/resources/munit/examples/sample-file.xml";

    @Test
    public void setNullSelector() throws Exception {
        migrationTask = new MigrationTask(null);
        migrationTask.execute();
        assertEquals(0, getListSize(migrationTask));
    }

    @Test
    public void setEmptySelector() throws Exception {
        migrationTask = new MigrationTask("");
        migrationTask.execute();
        assertEquals(0, getListSize(migrationTask));
    }

    @Test
    public void setSelectorForNoNode() throws Exception {
        migrationTask = new MigrationTask("//pepe");
        migrationTask.setDocument(getDocument(EXAMPLE_FILE_PATH));
        migrationTask.execute();
        assertEquals(0, getListSize(migrationTask));
    }

    @Test
    public void addNullStepToTask() throws Exception {
        migrationTask = new MigrationTask("//pepe");
        migrationTask.setDocument(getDocument(EXAMPLE_FILE_PATH));
        migrationTask.addStep(null);
        migrationTask.execute();
        assertEquals(0, getListSize(migrationTask));
    }

    @Test
    public void setSelectorForNodes() throws Exception {
        migrationTask = new MigrationTask("//munit:test");
        migrationTask.setDocument(getDocument(EXAMPLE_FILE_PATH));
        migrationTask.execute();
        assertEquals(7, getListSize(migrationTask));
    }

    @Test
    public void setSelectorForNodesAndExecuteStep() throws Exception {
        migrationTask = new MigrationTask("//munit:test");
        migrationTask.setDocument(getDocument(EXAMPLE_FILE_PATH));
        MigrationStep step = new AddAttribute("pepe", "pepa");
        migrationTask.addStep(step);
        migrationTask.execute();
        assertEquals(7, getListSize(migrationTask));
    }

    @Test
    public void setSelectorForNodesAndExecuteStepEmptyDoc() throws Exception {
        migrationTask = new MigrationTask("//munit:test");
        MigrationStep attStep = new AddAttribute("pepe", "test");
        migrationTask.addStep(attStep);
        migrationTask.execute();
        assertEquals(0, getListSize(migrationTask));
    }


    public int getListSize(MigrationTask task) throws Exception{
        int size;
        Field field = task.getClass().getDeclaredField("nodes");
        field.setAccessible(true);
        size = ((List)field.get(task)).size();
        return size;
    }
}
