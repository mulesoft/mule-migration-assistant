/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
