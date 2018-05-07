/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.file;

import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.getMigrationScriptFolder;
import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.library;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateOperationStructure;
import static com.mulesoft.tools.migration.xml.AdditionalNamespaces.FILE;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.toList;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.ExpressionMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.io.IOException;
import java.util.List;

/**
 * Migrates the outbound endpoints of the file transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FileOutboundEndpoint extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = "/mule:mule//file:outbound-endpoint";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update File outbound endpoints.";
  }

  public FileOutboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Namespace fileNs = Namespace.getNamespace(FILE.prefix(), FILE.uri());

    object.setName("write");

    List<Element> transformerChildren =
        object.getChildren().stream().filter(c -> c.getName().contains("transformer")).collect(toList());

    transformerChildren.forEach(tc -> {
      tc.getParent().removeContent(tc);
      object.getParentElement().addContent(object.getParentElement().indexOf(object) + 1, tc);
    });

    migrateOperationStructure(getApplicationModel(), object, report);

    object.setAttribute("path", compatibilityOutputFile("{"
        + " writeToDirectory: "
        + (object.getAttribute("path") == null ? propToDwExpr(object, "writeToDirectory")
            : "'" + object.getAttributeValue("path") + "'")
        + ","
        + " address: "
        + (object.getAttribute("address") != null
            ? ("'" + object.getAttributeValue("address").substring("file://".length()) + "'")
            : "null")
        + ","
        + " outputPattern: " + propToDwExpr(object, "outputPattern") + ","
        + " outputPatternConfig: " + propToDwExpr(object, "outputPatternConfig")
        + "}"));

    if (object.getAttribute("connector-ref") != null) {
      object.getAttribute("connector-ref").setName("config-ref");
    }

    object.removeAttribute("writeToDirectory");
    object.removeAttribute("outputPattern");
    object.removeAttribute("outputPatternConfig");
  }

  private String compatibilityOutputFile(String pathDslParams) {
    try {
      library(getMigrationScriptFolder(getApplicationModel().getProjectBasePath()), "FileWriteOutputFile.dwl",
              "" +
                  "/**" + lineSeparator() +
                  " * Emulates the outbound endpoint logic for determining the output filename of the Mule 3.x File transport."
                  + lineSeparator() +
                  " */" + lineSeparator() +
                  "fun fileWriteOutputfile(vars: {}, pathDslParams: {}) = do {" + lineSeparator() +
                  "    ((vars.compatibility_outboundProperties['writeToDirectoryName']" + lineSeparator() +
                  "        default pathDslParams.writeToDirectory)" + lineSeparator() +
                  "        default pathDslParams.address)" + lineSeparator() +
                  "    ++ '/' ++" + lineSeparator() +
                  "    (((vars.compatibility_outboundProperties.outputPattern" + lineSeparator() +
                  "        default pathDslParams.outputPattern)" + lineSeparator() +
                  "        default pathDslParams.outputPatternConfig)" + lineSeparator() +
                  "        default vars.compatibility_inboundProperties.filename" + lineSeparator() +
                  "    as String)" + lineSeparator() +
                  "}" + lineSeparator() +
                  lineSeparator());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return "#[migration::FileWriteOutputFile::fileWriteOutputfile(vars, " + pathDslParams + ")]";
  }

  private String propToDwExpr(Element object, String propName) {
    if (object.getAttribute(propName) != null) {
      if (getExpressionMigrator().isWrapped(object.getAttributeValue(propName))) {
        return getExpressionMigrator().migrateExpression(object.getAttributeValue(propName), true, object);
      } else {
        return "'" + object.getAttributeValue(propName) + "'";
      }
    } else {
      return "null";
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
