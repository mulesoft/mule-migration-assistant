/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.munit.steps;

import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class MUnitNamespacesTest {

  private static final String MUNIT_SAMPLE_XML = "munit-processors.xml";
  private static final Path MUNIT_EXAMPLES_PATH = Paths.get("munit/examples");
  private static final Path MUNIT_SAMPLE_PATH = MUNIT_EXAMPLES_PATH.resolve(MUNIT_SAMPLE_XML);

  @Rule
  public ReportVerification report = new ReportVerification();

  private MUnitNamespaces mUnitNamespaces;

  @Before
  public void setUp() throws Exception {
    mUnitNamespaces = new MUnitNamespaces();
  }

  @Test(expected = MigrationStepException.class)
  public void executeWithNullElement() throws Exception {
    mUnitNamespaces.execute(null, report.getReport());
  }

  @Test
  public void execute() throws Exception {

    mUnitNamespaces.execute(mock(ApplicationModel.class), report.getReport());

    //        assertThat("The node didn't change", node.getName(), is("assert-that"));
  }
}
