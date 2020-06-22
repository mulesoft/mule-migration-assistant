/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.file;

import static com.mulesoft.tools.migration.library.mule.steps.core.properties.InboundPropertiesHelper.addAttributesMapping;
import static com.mulesoft.tools.migration.library.mule.steps.file.FileConfig.FILE_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.file.FileConfig.FILE_NAMESPACE_URI;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateInboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateSchedulingStrategy;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addMigrationAttributeToElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.changeDefault;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateRedeliveryPolicyChildren;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

/**
 * Migrates the inbound endpoints of the file transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FileInboundEndpoint extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR =
      "/*/mule:flow/*[namespace-uri()='" + FILE_NAMESPACE_URI + "' and local-name()='inbound-endpoint'][1]";

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
    object.setName("listener");
    addMigrationAttributeToElement(object, new Attribute("isMessageSource", "true"));

    addAttributesToInboundProperties(object, report);

    Element redelivery = object.getChild("idempotent-redelivery-policy", CORE_NAMESPACE);
    if (redelivery != null) {
      redelivery.setName("redelivery-policy");
      Attribute exprAttr = redelivery.getAttribute("idExpression");

      if (exprAttr != null) {
        // TODO MMT-128
        exprAttr.setValue(exprAttr.getValue().replaceAll("#\\[header\\:inbound\\:originalFilename\\]", "#[attributes.name]"));

        if (getExpressionMigrator().isWrapped(exprAttr.getValue())) {
          exprAttr.setValue(getExpressionMigrator()
              .wrap(getExpressionMigrator().migrateExpression(exprAttr.getValue(), true, object)));
        }
      }

      migrateRedeliveryPolicyChildren(redelivery, report);
    }

    migrateSchedulingStrategy(object, OptionalInt.of(1000));

    if (object.getAttribute("fileAge") != null && !"0".equals(object.getAttributeValue("fileAge"))) {
      String fileAge = object.getAttributeValue("fileAge");
      object.setAttribute("timeBetweenSizeCheck", fileAge);
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

    migrateFileFilters(object, report, FILE_NAMESPACE, getApplicationModel());

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
      report.report("file.responseTimeout", object, object);
      object.removeAttribute("responseTimeout");
    }

    if (object.getAttribute("comparator") != null || object.getAttribute("reverseOrder") != null) {
      report.report("file.comparator", object, object);
      object.removeAttribute("comparator");
      object.removeAttribute("reverseOrder");
    }

    if (object.getAttribute("name") != null) {
      object.removeAttribute("name");
    }

    if (object.getAttribute("exchange-pattern") != null) {
      object.removeAttribute("exchange-pattern");
    }
  }

  public static void migrateFileFilters(Element object, MigrationReport report, Namespace ns, ApplicationModel appModel) {
    Element newMatcher = null;

    Element globFilterIn = object.getChild("filename-wildcard-filter", FILE_NAMESPACE);
    if (globFilterIn != null) {
      if (newMatcher == null) {
        newMatcher = buildNewMatcher(object, ns, appModel);
      }
      newMatcher.setAttribute("filenamePattern", globFilterIn.getAttributeValue("pattern"));

      if (globFilterIn.getAttribute("caseSensitive") != null) {
        report.report("file.caseSensitive", globFilterIn, newMatcher);
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

    Element regexFilterIn = object.getChild("filename-regex-filter", FILE_NAMESPACE);
    if (regexFilterIn != null) {
      if (newMatcher == null) {
        newMatcher = buildNewMatcher(object, ns, appModel);
      }
      newMatcher.setAttribute("filenamePattern", "regex:" + regexFilterIn.getAttributeValue("pattern"));

      if (regexFilterIn.getAttribute("caseSensitive") != null) {
        report.report("file.caseSensitive", regexFilterIn, newMatcher);
        regexFilterIn.removeAttribute("caseSensitive");
      }

      object.removeContent(regexFilterIn);
    }

    Element globFilter = object.getParentElement().getChild("filename-wildcard-filter", FILE_NAMESPACE);
    if (globFilter != null) {
      if (newMatcher == null) {
        newMatcher = buildNewMatcher(object, ns, appModel);
      }
      newMatcher.setAttribute("filenamePattern", globFilter.getAttributeValue("pattern"));

      if (globFilter.getAttribute("caseSensitive") != null) {
        report.report("file.caseSensitive", globFilter, newMatcher);
        globFilter.removeAttribute("caseSensitive");
      }

      object.getParentElement().removeContent(globFilter);
    }

    Element regexFilter = object.getParentElement().getChild("filename-regex-filter", FILE_NAMESPACE);
    if (regexFilter != null) {
      if (newMatcher == null) {
        newMatcher = buildNewMatcher(object, ns, appModel);
      }
      newMatcher.setAttribute("filenamePattern", "regex:" + regexFilter.getAttributeValue("pattern"));

      if (regexFilter.getAttribute("caseSensitive") != null) {
        report.report("file.caseSensitive", regexFilter, newMatcher);
        regexFilter.removeAttribute("caseSensitive");
      }

      object.getParentElement().removeContent(regexFilter);
    }
  }

  private static Element buildNewMatcher(Element object, Namespace ns, ApplicationModel appModel) {
    Element newMatcher;
    newMatcher = new Element("matcher", ns);

    List<Element> referencedMatcher =
        appModel.getNodes("/*/" + ns.getPrefix() + ":matcher[@name='" + object.getAttributeValue("matcher") + "']");
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
