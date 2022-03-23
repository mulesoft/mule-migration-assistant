/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.apikit.steps;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.util.XmlDslUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_EE_NAMESPACE;

/**
 * Migrates APIkit URI Params (map to variables)
 * @author Mulesoft Inc.
 * @since 1.2.1
 */
public class ApikitUriParams extends AbstractApikitMigrationStep {

  private static final String XPATH_SELECTOR = "//*[local-name()='flow' and namespace-uri()='" + XmlDslUtils.CORE_NS_URI
      + "' and contains(@name, '(') and contains(@name, ')')]";

  private static final String DOCS_NAMESPACE_URL = "http://www.mulesoft.org/schema/mule/documentation";
  private static final String DOCS_NAMESPACE_PREFIX = "doc";
  private static final Namespace DOCS_NAMESPACE = Namespace.getNamespace(DOCS_NAMESPACE_PREFIX, DOCS_NAMESPACE_URL);


  public ApikitUriParams() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    addVariableDeclarationFor(element, getUriParamsFrom(element));
  }

  private List<String> getUriParamsFrom(Element flow) {
    List<String> result = new ArrayList<>();

    String flowName = flow.getAttributeValue("name");

    // check if the flow ends with ":<STRING>" which should be the case for an apikit flow
    if (!StringUtils.isBlank(flowName) && flowName.matches(".*:.*$")) {
      Matcher m = Pattern.compile("\\(.*?\\)")
          .matcher(flowName);
      while (m.find()) {
        String uriParam = m.group().replaceAll("\\(|\\)", "");
        result.add(uriParam);
      }
    }

    return result;
  }


  private void addVariableDeclarationFor(Element flow, List<String> uriParams) {
    if (!uriParams.isEmpty()) {
      Element transformNode = addTransformNodeAsFirstNodeTo(flow);
      Element variables = (Element) transformNode.getContent(0);
      for (String uriParam : uriParams) {
        addVariable(variables, uriParam);
      }
    }
  }

  private Element addTransformNodeAsFirstNodeTo(Element flow) {
    Element result = createTransformNode();
    flow.addContent(0, result);
    return result;
  }

  private Element createTransformNode() {
    Element result = new Element("transform")
        .setName("transform")
        .setNamespace(CORE_EE_NAMESPACE)
        .setAttribute("name", "URI Params to Variables", DOCS_NAMESPACE);
    result.addContent(new Element("variables", CORE_EE_NAMESPACE));
    return result;
  }

  private void addVariable(Element variables, String uriParam) {
    Element variable = new Element("set-variable", CORE_EE_NAMESPACE)
        .setAttribute("variableName", uriParam);
    variable.addContent("attributes.uriParams." + uriParam);
    variables.addContent(variable);
  }

}
