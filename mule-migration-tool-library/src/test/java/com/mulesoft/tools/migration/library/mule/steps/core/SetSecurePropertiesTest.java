/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.google.common.collect.Lists.newArrayList;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.mulesoft.tools.migration.project.model.artifact.MuleArtifactJsonModel;
import com.mulesoft.tools.migration.project.model.artifact.MuleArtifactJsonModelUtils;
import com.mulesoft.tools.migration.tck.ReportVerification;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class SetSecurePropertiesTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private Path projectBasePath;
  private SetSecureProperties setSecureProperties;
  private File muleArtifactJsonFile;
  private final String MIN_MULE_VERSION = "4.1.2";
  private File muleAppProperties;

  @Before
  public void setUp() throws IOException {
    projectBasePath = temporaryFolder.getRoot().toPath();
    File resources = new File(projectBasePath.toFile(), "src/main/resources");
    resources.mkdirs();
    muleAppProperties = new File(resources, "mule-app.properties");
    FileUtils.write(muleAppProperties, "secure.properties=lala, pepe", UTF_8);
    setSecureProperties = new SetSecureProperties();
    muleArtifactJsonFile = new File(projectBasePath.toFile(), "mule-artifact.json");
    muleArtifactJsonFile.createNewFile();
    FileUtils.write(muleArtifactJsonFile, "{ minMuleVersion: " + MIN_MULE_VERSION + " }", UTF_8);

  }

  @Test
  public void execute() throws IOException {
    setSecureProperties.execute(projectBasePath, report.getReport());
    MuleArtifactJsonModel model = MuleArtifactJsonModelUtils.buildMuleArtifactJson(muleArtifactJsonFile.toPath());

    assertThat("Secure properties were not created successfully", model.getSecureProperties().get(),
               equalTo(newArrayList("lala", "pepe")));

    Properties properties = new Properties();
    properties.load(new FileInputStream(muleAppProperties));
    assertThat("Secure properties should not exist", properties.containsKey("secure.properties"), is(false));

  }
}
