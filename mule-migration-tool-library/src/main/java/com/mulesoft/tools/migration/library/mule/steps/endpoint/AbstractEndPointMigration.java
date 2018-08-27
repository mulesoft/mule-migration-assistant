/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.endpoint;

import com.mulesoft.tools.migration.library.mule.steps.file.FileInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.file.FileOutboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpOutboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpsInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpsOutboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.jms.JmsInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.jms.JmsOutboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.vm.VmInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.vm.VmOutboundEndpoint;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates address of generic endpoints.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractEndPointMigration extends AbstractApplicationModelMigrationStep {

  private static final String HTTP_NS_PREFIX = "http";
  private static final String HTTP_NS_URI = "http://www.mulesoft.org/schema/mule/http";
  private static final String HTTPS_NS_PREFIX = "https";
  private static final String HTTPS_NS_URI = "http://www.mulesoft.org/schema/mule/https";
  private static final String FILE_NS_PREFIX = "file";
  private static final String FILE_NS_URI = "http://www.mulesoft.org/schema/mule/file";
  private static final String JMS_NS_PREFIX = "jms";
  private static final String JMS_NS_URI = "http://www.mulesoft.org/schema/mule/jms";
  private static final String VM_NS_PREFIX = "vm";
  private static final String VM_NS_URI = "http://www.mulesoft.org/schema/mule/vm";

  public AbstractApplicationModelMigrationStep getInboundMigrator(String addressValue, Element element) {
    AbstractApplicationModelMigrationStep migrator = null;
    if (addressValue.startsWith("file://")) {
      migrator = new FileInboundEndpoint();
      element.setNamespace(Namespace.getNamespace(FILE_NS_PREFIX, FILE_NS_URI));
    } else if (addressValue.startsWith("http://")) {
      migrator = new HttpInboundEndpoint();
      element.setNamespace(Namespace.getNamespace(HTTP_NS_PREFIX, HTTP_NS_URI));
    } else if (addressValue.startsWith("https://")) {
      migrator = new HttpsInboundEndpoint();
      element.setNamespace(Namespace.getNamespace(HTTPS_NS_PREFIX, HTTPS_NS_URI));
    } else if (addressValue.startsWith("jms://")) {
      migrator = new JmsInboundEndpoint();
      element.setNamespace(Namespace.getNamespace(JMS_NS_PREFIX, JMS_NS_URI));
    } else if (addressValue.startsWith("vm://")) {
      migrator = new VmInboundEndpoint();
      element.setNamespace(Namespace.getNamespace(VM_NS_PREFIX, VM_NS_URI));
    }
    return migrator;
  }

  public AbstractApplicationModelMigrationStep getOutboundMigrator(String addressValue, Element element) {
    AbstractApplicationModelMigrationStep migrator = null;
    if (addressValue.startsWith("file://")) {
      migrator = new FileOutboundEndpoint();
      element.setNamespace(Namespace.getNamespace(FILE_NS_PREFIX, FILE_NS_URI));
    } else if (addressValue.startsWith("http://")) {
      migrator = new HttpOutboundEndpoint();
      element.setNamespace(Namespace.getNamespace(HTTP_NS_PREFIX, HTTP_NS_URI));
    } else if (addressValue.startsWith("https://")) {
      migrator = new HttpsOutboundEndpoint();
      element.setNamespace(Namespace.getNamespace(HTTPS_NS_PREFIX, HTTPS_NS_URI));
    } else if (addressValue.startsWith("jms://")) {
      migrator = new JmsOutboundEndpoint();
      element.setNamespace(Namespace.getNamespace(JMS_NS_PREFIX, JMS_NS_URI));
    } else if (addressValue.startsWith("vm://")) {
      migrator = new VmOutboundEndpoint();
      element.setNamespace(Namespace.getNamespace(VM_NS_PREFIX, VM_NS_URI));
    }
    return migrator;
  }
}
