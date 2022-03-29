/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.apikit.steps;

import com.mulesoft.tools.migration.library.apikit.ApikitUriParamUtils;
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
public class ApikitFlowUriParams extends AbstractApikitMigrationStep {

  private static final String XPATH_SELECTOR = "//*[local-name()='flow' and namespace-uri()='" + XmlDslUtils.CORE_NS_URI
      + "' and contains(@name, '\\(') and contains(@name, ')')]";

  public ApikitFlowUriParams() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    ApikitUriParamUtils.addVariableDeclarationFor(element, getUriParamsFrom(element));
  }

  @Override
  public boolean shouldReportMetrics() {
    return false;
  }

  private List<String> getUriParamsFrom(Element flow) {
    List<String> result = new ArrayList<>();

    String flowName = flow.getAttributeValue("name");

    if (!StringUtils.isBlank(flowName)) {
      Matcher m = Pattern.compile("\\\\\\(.*?\\)")
          .matcher(flowName);
      while (m.find()) {
        String uriParam = m.group().replaceAll("\\\\\\(|\\)", "");
        result.add(uriParam);
      }
    }

    return result;
  }
}
