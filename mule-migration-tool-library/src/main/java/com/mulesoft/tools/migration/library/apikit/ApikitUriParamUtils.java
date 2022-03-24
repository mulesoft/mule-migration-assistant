/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.apikit;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_EE_NAMESPACE;

/**
 * Util to insert a transform element in a flow for Uri param cases
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ApikitUriParamUtils {

  private static final String DOCS_NAMESPACE_URL = "http://www.mulesoft.org/schema/mule/documentation";
  private static final String DOCS_NAMESPACE_PREFIX = "doc";
  private static final Namespace DOCS_NAMESPACE = Namespace.getNamespace(DOCS_NAMESPACE_PREFIX, DOCS_NAMESPACE_URL);

  public static void addVariableDeclarationFor(Element flow, List<String> uriParams) {
    if (!uriParams.isEmpty()) {
      Element transformNode = addTransformNodeAsFirstNodeTo(flow);
      Element variables = (Element) transformNode.getContent(0);
      for (String uriParam : uriParams) {
        addVariable(variables, uriParam);
      }
    }
  }

  private static Element addTransformNodeAsFirstNodeTo(Element flow) {
    Element result = createTransformNode();
    flow.addContent(0, result);
    return result;
  }

  private static Element createTransformNode() {
    Element result = new Element("transform")
        .setName("transform")
        .setNamespace(CORE_EE_NAMESPACE)
        .setAttribute("name", "URI Params to Variables", DOCS_NAMESPACE);
    result.addContent(new Element("variables", CORE_EE_NAMESPACE));
    return result;
  }

  private static void addVariable(Element variables, String uriParam) {
    Element variable = new Element("set-variable", CORE_EE_NAMESPACE)
        .setAttribute("variableName", uriParam);
    variable.addContent("attributes.uriParams." + uriParam);
    variables.addContent(variable);
  }

}
