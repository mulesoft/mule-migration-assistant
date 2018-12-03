/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.quartz;

import static com.mulesoft.tools.migration.library.mule.steps.jms.AbstractJmsEndpoint.JMS_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.jms.AbstractJmsEndpoint.addAttributesToInboundProperties;
import static com.mulesoft.tools.migration.library.mule.steps.jms.AbstractJmsEndpoint.migrateJmsConfig;
import static com.mulesoft.tools.migration.library.mule.steps.jms.AbstractJmsEndpoint.resolveJmsConnector;
import static com.mulesoft.tools.migration.library.mule.steps.jms.JmsOutboundEndpoint.migrateOutboundJmsEndpoint;
import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.VM_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.getVmConfigName;
import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.migrateVmConfig;
import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.resolveVmConector;
import static com.mulesoft.tools.migration.library.mule.steps.vm.VmOutboundEndpoint.migrateVmEndpointConsumer;
import static com.mulesoft.tools.migration.step.AbstractGlobalEndpointMigratorStep.copyAttributes;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.extractInboundChildren;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateOutboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementBefore;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addMigrationAttributeToElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getFlow;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.ElementFilter;

import java.util.Optional;

/**
 * Migrates the inbound endpoints of the quartz transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class QuartzInboundEndpoint extends AbstractApplicationModelMigrationStep {

  protected static final String QUARTZ_NS_PREFIX = "quartz";
  protected static final String QUARTZ_NS_URI = "http://www.mulesoft.org/schema/mule/quartz";
  private static final Namespace QUARTZ_NS = getNamespace(QUARTZ_NS_PREFIX, QUARTZ_NS_URI);

  public static final String XPATH_SELECTOR =
      "/*/mule:flow/*[namespace-uri() = '" + QUARTZ_NS_URI + "' and local-name() = 'inbound-endpoint'][1]";

  @Override
  public String getDescription() {
    return "Update quartz inbound endpoints.";
  }

  public QuartzInboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    if (object.removeAttribute("repeatCount")) {
      report.report("quartz.repeatCount", object, object);
    }

    object.setName("scheduler");
    object.setNamespace(CORE_NAMESPACE);
    addMigrationAttributeToElement(object, new Attribute("isMessageSource", "true"));

    String configName = object.getAttributeValue("connector-ref");
    Optional<Element> config;
    if (configName != null) {
      config = getApplicationModel().getNodeOptional("/*/*[namespace-uri() = '" + QUARTZ_NS_URI
          + "' and local-name() = 'connector' and @name = '" + configName + "']");
    } else {
      config =
          getApplicationModel().getNodeOptional("/*/*[namespace-uri() = '" + QUARTZ_NS_URI + "' and local-name() = 'connector']");
    }

    config.ifPresent(cfg -> {
      Element receiverThreadingProfile = cfg.getChild("receiver-threading-profile", CORE_NAMESPACE);

      if (receiverThreadingProfile != null) {
        if (receiverThreadingProfile.getAttribute("maxThreadsActive") != null) {
          getFlow(object).setAttribute("maxConcurrency", receiverThreadingProfile.getAttributeValue("maxThreadsActive"));
        }
      }
    });

    Element schedulingStrategy = new Element("scheduling-strategy", CORE_NAMESPACE);

    Element fixedFrequecy = null;
    Element cron = null;

    if (object.getAttribute("repeatInterval") != null) {
      if (fixedFrequecy == null) {
        fixedFrequecy = new Element("fixed-frequency", CORE_NAMESPACE);
        schedulingStrategy.addContent(fixedFrequecy);
      }

      fixedFrequecy.setAttribute("frequency", object.getAttributeValue("repeatInterval"));
      object.removeAttribute("repeatInterval");
    }
    if (object.getAttribute("startDelay") != null) {
      if (fixedFrequecy == null) {
        fixedFrequecy = new Element("fixed-frequency", CORE_NAMESPACE);
        schedulingStrategy.addContent(fixedFrequecy);
      }

      fixedFrequecy.setAttribute("startDelay", object.getAttributeValue("startDelay"));
      object.removeAttribute("startDelay");
    }

    if (object.getAttribute("cronExpression") != null) {
      if (cron == null) {
        cron = new Element("cron", CORE_NAMESPACE);
        schedulingStrategy.addContent(cron);
      }

      cron.setAttribute("expression", object.getAttributeValue("cronExpression"));
      object.removeAttribute("cronExpression");
    }
    if (object.getAttribute("cronTimeZone") != null) {
      if (cron == null) {
        cron = new Element("cron", CORE_NAMESPACE);
        schedulingStrategy.addContent(cron);
      }

      cron.setAttribute("timeZone", object.getAttributeValue("cronTimeZone"));
      object.removeAttribute("cronTimeZone");
    }

    Element eventGeneratorJob = object.getChild("event-generator-job", QUARTZ_NS);
    if (eventGeneratorJob != null) {
      String payload = eventGeneratorJob.getChildText("payload", QUARTZ_NS);
      if (payload != null) {
        addElementAfter(new Element("set-payload", CORE_NAMESPACE)
            .setAttribute("value", payload), object);
      }
    }

    Element endpointPollingJob = object.getChild("endpoint-polling-job", QUARTZ_NS);
    if (endpointPollingJob != null) {
      Element jobEndpoint = endpointPollingJob.getChild("job-endpoint", QUARTZ_NS);
      addMigrationAttributeToElement(jobEndpoint, new Attribute("isPolledConsumer", "true"));

      if (JobEndpointMigrableConnector.JMS.equals(resolveEndpointConnector(jobEndpoint))) {
        handleGlobalEndpointsRefs(jobEndpoint);
        final Optional<Element> jobEndpointConnector = resolveJmsConnector(jobEndpoint, getApplicationModel());
        jobEndpointConnector.ifPresent(c -> jobEndpoint.setAttribute("connector-ref", c.getAttributeValue("name")));

        getApplicationModel().addNameSpace(JMS_NAMESPACE, "http://www.mulesoft.org/schema/mule/jms/current/mule-jms.xsd",
                                           object.getDocument());
        jobEndpoint.setNamespace(JMS_NAMESPACE);
        jobEndpoint.setName("consume");

        String jmsConfig = migrateJmsConfig(jobEndpoint, report, jobEndpointConnector, getApplicationModel());
        migrateOutboundJmsEndpoint(jobEndpoint, report, jobEndpointConnector, jmsConfig, getApplicationModel());

        jobEndpoint.detach();
        addElementAfter(jobEndpoint, object);

        if (object.getAttribute("timeout") != null) {
          jobEndpoint.addContent(new Element("consume-configuration", JMS_NAMESPACE)
              .setAttribute("maximumWait", object.getAttributeValue("timeout")));
        }

        migrateOutboundEndpointStructure(getApplicationModel(), jobEndpoint, report, true);
        extractInboundChildren(jobEndpoint, jobEndpoint.getParentElement().indexOf(jobEndpoint) + 2,
                               jobEndpoint.getParentElement(), getApplicationModel());
        addAttributesToInboundProperties(jobEndpoint, getApplicationModel(), report);

        object.addContent(schedulingStrategy);
      } else if (JobEndpointMigrableConnector.VM.equals(resolveEndpointConnector(jobEndpoint))) {
        handleGlobalEndpointsRefs(jobEndpoint);
        final Optional<Element> jobEndpointConnector = resolveVmConector(jobEndpoint, getApplicationModel());
        jobEndpointConnector.ifPresent(c -> jobEndpoint.setAttribute("connector-ref", c.getAttributeValue("name")));

        getApplicationModel().addNameSpace(VM_NAMESPACE, "http://www.mulesoft.org/schema/mule/vm/current/mule-vm.xsd",
                                           object.getDocument());
        jobEndpoint.setNamespace(VM_NAMESPACE);
        jobEndpoint.setName("consume");

        final String vmConfigName = getVmConfigName(object, jobEndpointConnector);
        Element vmConfig = migrateVmConfig(object, jobEndpointConnector, vmConfigName, getApplicationModel());
        migrateVmEndpointConsumer(jobEndpoint, report, jobEndpointConnector, vmConfigName, vmConfig);

        jobEndpoint.detach();
        addElementAfter(jobEndpoint, object);

        if (object.getAttribute("timeout") != null) {
          jobEndpoint.setAttribute("timeout", object.getAttributeValue("timeout"));
          jobEndpoint.setAttribute("timeoutUnit", "MILLISECONDS");
          object.removeAttribute("timeout");
        }

        migrateOutboundEndpointStructure(getApplicationModel(), jobEndpoint, report, true, true);
        extractInboundChildren(jobEndpoint, jobEndpoint.getParentElement().indexOf(jobEndpoint) + 2,
                               jobEndpoint.getParentElement(), getApplicationModel());

        object.addContent(schedulingStrategy);
      } else if (JobEndpointMigrableConnector.POLLING.equals(resolveEndpointConnector(jobEndpoint))) {
        // This handles a Mule 4 connector source that is a polling source

        jobEndpoint.setName("inbound-endpoint");
        jobEndpoint.setNamespace(CORE_NAMESPACE);

        jobEndpoint.detach();
        addElementBefore(jobEndpoint, object);
        jobEndpoint.addContent(schedulingStrategy);
        object.detach();
      } else {
        object.addContent(schedulingStrategy);
        report.report("quartz.unsupportedSource", jobEndpoint, object);
      }
    } else {
      object.addContent(schedulingStrategy);
    }

    object.removeAttribute("connector-ref");
    object.removeAttribute("name");
    object.removeAttribute("jobName");

    object.removeContent(new ElementFilter(QUARTZ_NS));
  }

  private JobEndpointMigrableConnector resolveEndpointConnector(Element endpoint) {
    if (endpoint.getAttribute("address") != null) {
      final String address = endpoint.getAttributeValue("address");
      if (address.startsWith("vm://")) {
        // Don't fully understand why would anybody be using quartz instead of a listener, but well...
        return JobEndpointMigrableConnector.VM;
      } else if (address.startsWith("jms://")) {
        // Don't fully understand why would anybody be using quartz instead of a listener, but well...
        return JobEndpointMigrableConnector.JMS;
      } else if (address.startsWith("imap://")
          || address.startsWith("imaps://")
          || address.startsWith("pop3://")
          || address.startsWith("pop3s://")
          || address.startsWith("file://")
          || address.startsWith("ftp://")
          || address.startsWith("sftp://")) {
        // TODO MMT-159 Populate this list in runtime. Provided migrators may add to it.
        return JobEndpointMigrableConnector.POLLING;
      }
    }

    if (endpoint.getAttribute("ref") != null) {
      Element globalEndpoint = getApplicationModel().getNode("/*/*[@name = '" + endpoint.getAttributeValue("ref") + "']");
      return resolveEndpointConnector(globalEndpoint);
    }

    return JobEndpointMigrableConnector.OTHER;
  }

  protected void handleGlobalEndpointsRefs(final Element endpoint) {
    if (endpoint.getAttribute("ref") != null) {
      Element globalEndpoint = getApplicationModel().getNode("/*/*[@name = '" + endpoint.getAttributeValue("ref") + "']");
      copyAttributes(globalEndpoint, endpoint);
      endpoint.removeAttribute("ref");
    }
  }

  private static enum JobEndpointMigrableConnector {
    JMS, VM, POLLING, OTHER
  }

}
