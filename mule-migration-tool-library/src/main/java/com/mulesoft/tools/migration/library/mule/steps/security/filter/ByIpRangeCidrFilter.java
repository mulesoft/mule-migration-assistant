/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.security.filter;

import static com.mulesoft.tools.migration.library.mule.steps.validation.ValidationPomContribution.addValidationDependency;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getContainerElement;
import static java.util.Collections.singletonList;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.library.mule.steps.core.filter.AbstractFilterMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Update filter-by-ip-range-cidr filter to use the validations module.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ByIpRangeCidrFilter extends AbstractFilterMigrator {

  private static final String FILTERS_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/filters";
  private static final Namespace FILTERS_NAMESPACE = getNamespace("filters", FILTERS_NAMESPACE_URI);

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri() = '" + FILTERS_NAMESPACE_URI + "' and local-name() = 'filter-by-ip-range-cidr']";

  @Override
  public String getDescription() {
    return "Update filter-by-ip-range-cidr filter to use the validations module.";
  }

  public ByIpRangeCidrFilter() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(singletonList(FILTERS_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    addValidationNamespace(element.getDocument());
    addValidationDependency(getApplicationModel().getPomModel().get());

    element.setName("is-whitelisted-ip");
    element.setNamespace(VALIDATION_NAMESPACE);

    element.setAttribute("ipAddress",
                         "#[if (attributes.headers['X-Forwarded-For'] != null) trim((attributes.headers['X-Forwarded-For'] splitBy  ',')[0]) else attributes.remoteAddress]");

    final String ipFilterListName = getContainerElement(element).getAttributeValue("name") + "_filter-by-ip-range-cidr";
    element.setAttribute("whiteList", ipFilterListName);

    final Element ipFilterList = new Element("ip-filter-list", VALIDATION_NAMESPACE)
        .setAttribute("name", ipFilterListName);
    final Element ips = new Element("ips", VALIDATION_NAMESPACE);

    ipFilterList.addContent(ips.addContent(new Element("ip", VALIDATION_NAMESPACE)
        .setAttribute("value", element.getAttributeValue("cidr"))));

    addTopLevelElement(ipFilterList, element.getDocument());

    element.removeAttribute("cidr");

    handleFilter(element);
  }

}
