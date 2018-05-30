/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.file;

import static com.mulesoft.tools.migration.library.mule.steps.core.properties.InboundPropertiesHelper.addAttributesMapping;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateInboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.changeDefault;
import static com.mulesoft.tools.migration.xml.AdditionalNamespaces.FILE;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.ExpressionMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Migrates the inbound endpoints of the file transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FileInboundEndpoint extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = "/mule:mule/mule:flow/file:inbound-endpoint[1]";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update File inbound endpoints.";
  }

  public FileInboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Namespace fileNs = Namespace.getNamespace(FILE.prefix(), FILE.uri());

    object.setName("listener");

    addAttributesToInboundProperties(object, report);

    Element redelivery = object.getChild("idempotent-redelivery-policy", CORE_NAMESPACE);
    if (redelivery != null) {
      redelivery.setName("redelivery-policy");
      Attribute exprAttr = redelivery.getAttribute("idExpression");

      exprAttr.setValue(exprAttr.getValue().replaceAll("#\\[header\\:inbound\\:originalFilename\\]", "#[attributes.name]"));

      if (getExpressionMigrator().isWrapped(exprAttr.getValue())) {
        exprAttr
            .setValue(getExpressionMigrator().wrap(getExpressionMigrator().migrateExpression(exprAttr.getValue(), true, object)));
      }
    }

    Element schedulingStr = object.getChild("scheduling-strategy", CORE_NAMESPACE);
    if (schedulingStr == null) {
      schedulingStr = new Element("scheduling-strategy", CORE_NAMESPACE);
      schedulingStr.addContent(new Element("fixed-frequency", CORE_NAMESPACE));
      object.addContent(schedulingStr);
    }

    Element fixedFrequency = schedulingStr.getChild("fixed-frequency", CORE_NAMESPACE);

    if (object.getAttribute("pollingFrequency") != null) {
      fixedFrequency.setAttribute("frequency", object.getAttributeValue("pollingFrequency", "1000"));
    } else if (fixedFrequency.getAttribute("frequency") == null) {
      fixedFrequency.setAttribute("frequency", "1000");
    }
    object.removeAttribute("pollingFrequency");

    Element newMatcher = null;

    if (object.getAttribute("fileAge") != null) {
      newMatcher = buildNewMatcher(object, fileNs);
      newMatcher.setAttribute("updatedUntil", object.getAttributeValue("fileAge"));
      object.removeAttribute("fileAge");
    }

    if (object.getAttribute("moveToPattern") != null) {
      String moveToPattern = object.getAttributeValue("moveToPattern");
      object.setAttribute("renameTo",
                          getExpressionMigrator().migrateExpression(moveToPattern, true, object));
      object.removeAttribute("moveToPattern");
    }

    // TODO test
    if (object.getAttribute("moveToDirectory") != null) {
      if ("true".equals(object.getAttributeValue("autoDelete"))) {
        object.removeAttribute("autoDelete");
      }
    }

    Element globFilterIn = object.getChild("filename-wildcard-filter", fileNs);
    if (globFilterIn != null) {
      if (newMatcher == null) {
        newMatcher = buildNewMatcher(object, fileNs);
      }
      newMatcher.setAttribute("filenamePattern", globFilterIn.getAttributeValue("pattern"));

      if (globFilterIn.getAttribute("caseSensitive") != null) {
        report.report(WARN, globFilterIn, newMatcher,
                      "'caseSensitive' is not supported in Mule 4 File Connector. The case sensitivity is delegated to the file system.",
                      "https://docs.mulesoft.com/connectors/file-on-new-file");
        globFilterIn.removeAttribute("caseSensitive");
      }

      object.removeContent(globFilterIn);
    }

    Element customFilterIn = object.getChild("custom-filter", COMPATIBILITY_NAMESPACE);
    if (customFilterIn != null) {
      object.removeContent(customFilterIn);
      // The ERROR will be reported when all custom-filters are queried to be migrated
      object.getParentElement().addContent(3, customFilterIn);
    }

    Element regexFilterIn = object.getChild("filename-regex-filter", fileNs);
    if (regexFilterIn != null) {
      if (newMatcher == null) {
        newMatcher = buildNewMatcher(object, fileNs);
      }
      newMatcher.setAttribute("filenamePattern", "regex:" + regexFilterIn.getAttributeValue("pattern"));

      if (regexFilterIn.getAttribute("caseSensitive") != null) {
        report.report(WARN, regexFilterIn, newMatcher,
                      "'caseSensitive' is not supported in Mule 4 File Connector. The case sensitivity is delegated to the file system.",
                      "https://docs.mulesoft.com/connectors/file-on-new-file");
        regexFilterIn.removeAttribute("caseSensitive");
      }

      object.removeContent(regexFilterIn);
    }

    Element globFilter = object.getParentElement().getChild("filename-wildcard-filter", fileNs);
    if (globFilter != null) {
      if (newMatcher == null) {
        newMatcher = buildNewMatcher(object, fileNs);
      }
      newMatcher.setAttribute("filenamePattern", globFilter.getAttributeValue("pattern"));

      if (globFilter.getAttribute("caseSensitive") != null) {
        report.report(WARN, globFilter, newMatcher,
                      "'caseSensitive' is not supported in Mule 4 File Connector. The case sensitivity is delegated to the file system.",
                      "https://docs.mulesoft.com/connectors/file-on-new-file");
        globFilter.removeAttribute("caseSensitive");
      }

      object.getParentElement().removeContent(globFilter);
    }

    Element regexFilter = object.getParentElement().getChild("filename-regex-filter", fileNs);
    if (regexFilter != null) {
      if (newMatcher == null) {
        newMatcher = buildNewMatcher(object, fileNs);
      }
      newMatcher.setAttribute("filenamePattern", "regex:" + regexFilter.getAttributeValue("pattern"));

      if (regexFilter.getAttribute("caseSensitive") != null) {
        report.report(WARN, regexFilter, newMatcher,
                      "'caseSensitive' is not supported in Mule 4 File Connector. The case sensitivity is delegated to the file system.",
                      "https://docs.mulesoft.com/connectors/file-on-new-file");
        regexFilter.removeAttribute("caseSensitive");
      }

      object.getParentElement().removeContent(regexFilter);
    }

    object.setAttribute("applyPostActionWhenFailed", "false");

    String recursive = changeDefault("false", "true", object.getAttributeValue("recursive"));
    if (recursive != null) {
      object.setAttribute("recursive", recursive);
    } else {
      object.removeAttribute("recursive");
    }

    processAddress(object, report).ifPresent(address -> {
      object.setAttribute("directory", address.getPath());
    });
    if (object.getAttribute("path") != null) {
      object.getAttribute("path").setName("directory");
    }
    if (object.getAttribute("connector-ref") != null) {
      object.getAttribute("connector-ref").setName("config-ref");
    } else {
      // Set the Mule 3 defaults since those are different in Mule 4
      object.setAttribute("autoDelete", "true");
      object.setAttribute("recursive", "false");
    }

    if (object.getAttribute("encoding") != null) {
      object.getParent().addContent(3, new Element("set-payload", CORE_NAMESPACE)
          .setAttribute("value", "#[payload]")
          .setAttribute("encoding", object.getAttributeValue("encoding")));
      object.removeAttribute("encoding");
    }
    if (object.getAttribute("responseTimeout") != null) {
      report.report(WARN, object, object, "'responseTimeout' was not being used by the file transport.");
      object.removeAttribute("responseTimeout");
    }

    if (object.getAttribute("comparator") != null || object.getAttribute("reverseOrder") != null) {
      report.report(ERROR, object, object,
                    "'comparator'/'reverseOrder' are not yet supported by the file connector listener.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-file#file_listener");
      object.removeAttribute("comparator");
      object.removeAttribute("reverseOrder");
    }

    if (object.getAttribute("name") != null) {
      object.removeAttribute("name");
    }
  }

  private Element buildNewMatcher(Element object, Namespace fileNs) {
    Element newMatcher;
    newMatcher = new Element("matcher", fileNs);

    List<Element> referencedMatcher =
        getApplicationModel().getNodes("/mule:mule/file:matcher[@name='" + object.getAttributeValue("matcher") + "']");
    if (!referencedMatcher.isEmpty()) {
      for (Attribute attribute : referencedMatcher.get(0).getAttributes()) {
        newMatcher.setAttribute(attribute.getName(), attribute.getValue());
      }
    }

    String newMatcherName =
        (object.getAttributeValue("connector-ref") != null ? object.getAttributeValue("connector-ref") + "-" : "")
            + object.getParentElement().getAttributeValue("name") + "Matcher";
    newMatcher.setAttribute("name", newMatcherName);
    object.setAttribute("matcher", newMatcherName);

    int idx = object.getDocument().getRootElement().indexOf(object.getParentElement());
    object.getDocument().getRootElement().addContent(idx, newMatcher);
    return newMatcher;
  }

  private void addAttributesToInboundProperties(Element object, MigrationReport report) {
    migrateInboundEndpointStructure(getApplicationModel(), object, report, true);

    Map<String, String> expressionsPerProperty = new LinkedHashMap<>();
    expressionsPerProperty.put("originalFilename", "message.attributes.fileName");
    expressionsPerProperty.put("originalDirectory",
                               "(message.attributes.path as String) [0 to -(2 + sizeOf(message.attributes.fileName))]");
    expressionsPerProperty.put("sourceFileName", "message.attributes.fileName");
    expressionsPerProperty.put("sourceDirectory",
                               "(message.attributes.path as String) [0 to -(2 + sizeOf(message.attributes.fileName))]");
    expressionsPerProperty.put("filename", "message.attributes.fileName");
    expressionsPerProperty.put("directory",
                               "(message.attributes.path as String) [0 to -(2 + sizeOf(message.attributes.fileName))]");
    expressionsPerProperty.put("fileSize", "message.attributes.size");
    expressionsPerProperty.put("timestamp", "message.attributes.lastModifiedTime");
    expressionsPerProperty.put("MULE.FORCE_SYNC", "false");

    try {
      addAttributesMapping(getApplicationModel(), "org.mule.extension.file.api.LocalFileAttributes", expressionsPerProperty);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }

}
