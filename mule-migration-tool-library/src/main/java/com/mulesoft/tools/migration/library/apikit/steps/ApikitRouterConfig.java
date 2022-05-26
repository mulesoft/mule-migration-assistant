/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.apikit.steps;

import static com.mulesoft.tools.migration.library.mule.steps.core.Flow.migrateFlowName;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static java.util.stream.Collectors.toList;

import com.mulesoft.tools.migration.library.apikit.ApikitUriParamUtils;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Migrates the router configuration of APIkit
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ApikitRouterConfig extends AbstractApikitMigrationStep {

  private static final String XPATH_SELECTOR = "//*[local-name()='config' and namespace-uri()='" + APIKIT_NAMESPACE_URI + "']";
  private static final String FLOW_XPATH_SELECTOR = "//*[@name='%s']";
  private static final String FLOW_MAPPING_PARENT_TAG_NAME = "flow-mappings";
  private static final String FLOW_MAPPING_TAG_NAME = "flow-mapping";
  private static final String FLOW_REF_ATTRIBUTE = "flow-ref";
  private static final String RESOURCE_ATTRIBUTE = "resource";

  @Override
  public String getDescription() {
    return "Update APIkit config";
  }

  public ApikitRouterConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) {
    // Add new attribures
    element.setAttribute("outboundHeadersMapName", "outboundHeaders");
    element.setAttribute("httpStatusVarName", "httpStatus");

    // Remove obsolete attributes
    if (element.getAttribute("consoleEnabled") != null) {
      report.report(WARN, element, element.getParentElement(), "consoleEnabled property do not exist in Mule 4.");
      element.removeAttribute("consoleEnabled");
    }

    if (element.getAttribute("consolePath") != null) {
      report.report(WARN, element, element.getParentElement(), "consolePath property do not exist in Mule 4.");
      element.removeAttribute("consolePath");
    }

    if (element.getAttribute("extensionEnabled") != null) {
      report.report(WARN, element, element.getParentElement(), "extensionEnabled property do not exist in Mule 4.");
      element.removeAttribute("extensionEnabled");
    }

    migrateFlowMappings(element, report);
  }

  private void migrateFlowMappings(Element config, MigrationReport report) {
    final List<Element> flowMappings = config.getChildren().stream()
        .filter(child -> APIKIT_NS_PREFIX.equals(child.getNamespacePrefix())
            && FLOW_MAPPING_TAG_NAME.equals(child.getName()))
        .collect(toList());

    if (!flowMappings.isEmpty()) {
      final Element flowMappingParent = getFlowMappingsParent(config);
      flowMappings.forEach(flowMapping -> {
        flowMapping.detach();
        Attribute flowRef = flowMapping.getAttribute("flow-ref");
        flowRef.setValue(migrateFlowName(flowRef.getValue()));
        flowMapping.setNamespace(APIKIT_NAMESPACE);
        flowMappingParent.addContent(flowMapping);
        migrateUriParams(flowMapping);
      });
    }
  }

  private void migrateUriParams(Element flowMapping) {
    Attribute flowRef = flowMapping.getAttribute(FLOW_REF_ATTRIBUTE);
    Attribute resourceUri = flowMapping.getAttribute(RESOURCE_ATTRIBUTE);
    Element flow = getApplicationModel().getNode(String.format(FLOW_XPATH_SELECTOR, flowRef.getValue()));

    if (flow != null && flowRef != null && resourceUri != null) {
      List<String> uriParams = new ArrayList<>();

      Matcher m = Pattern.compile("\\{.*?\\}").matcher(resourceUri.getValue());
      while (m.find()) {
        String uriParam = m.group().replaceAll("\\{|\\}", "");
        uriParams.add(uriParam);
      }

      ApikitUriParamUtils.addVariableDeclarationFor(flow, uriParams);
    }
  }

  private Element getFlowMappingsParent(Element config) {
    Element flowMappingsParent = config.getChild(FLOW_MAPPING_PARENT_TAG_NAME, APIKIT_NAMESPACE);
    if (flowMappingsParent == null) {
      flowMappingsParent = new Element(FLOW_MAPPING_PARENT_TAG_NAME, APIKIT_NAMESPACE);
      config.addContent(flowMappingsParent);
    }
    return flowMappingsParent;
  }
}
