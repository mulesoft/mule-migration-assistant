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
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.library.tools.PluginsVersions.targetVersion;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.Dependency.DependencyBuilder;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the configuration of the TCP/UDP Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SocketsConfig extends AbstractApplicationModelMigrationStep {

  private static final String TCP_NAMESPACE = "http://www.mulesoft.org/schema/mule/tcp";
  public static final String SOCKETS_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/sockets";
  public static final Namespace SOCKETS_NAMESPACE = Namespace.getNamespace("sockets", SOCKETS_NAMESPACE_URI);

  public static final String XPATH_SELECTOR = ""
      + "//*[namespace-uri()='" + TCP_NAMESPACE + "']";

  @Override
  public String getDescription() {
    return "Update Sockets config.";
  }

  public SocketsConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(SOCKETS_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.setNamespace(SOCKETS_NAMESPACE);

    if ("client-socket-properties".equals(object.getName())) {
      object.setNamespace(SOCKETS_NAMESPACE);
    }
  }

  public static void addSocketsModule(ApplicationModel applicationModel) {
    applicationModel.getPomModel().ifPresent(pom -> pom.addDependency(new DependencyBuilder()
        .withGroupId("org.mule.connectors")
        .withArtifactId("mule-sockets-connector")
        .withVersion(targetVersion("mule-sockets-connector"))
        .withClassifier("mule-plugin")
        .build()));

    applicationModel.addNameSpace("sockets", "http://www.mulesoft.org/schema/mule/sockets",
                                  "http://www.mulesoft.org/schema/mule/sockets/current/mule-sockets.xsd");
  }
}
