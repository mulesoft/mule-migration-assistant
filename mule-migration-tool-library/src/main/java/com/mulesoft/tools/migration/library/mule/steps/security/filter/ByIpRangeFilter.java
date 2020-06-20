/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.mulesoft.tools.migration.library.mule.steps.security.filter;

import static com.mulesoft.tools.migration.library.mule.steps.validation.ValidationPomContribution.addValidationDependency;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getContainerElement;
import static java.util.Collections.singletonList;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.library.mule.steps.core.filter.AbstractFilterMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.apache.commons.net.util.SubnetUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Update filter-by-ip-range filter to use the validations module.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ByIpRangeFilter extends AbstractFilterMigrator {

  private static final String FILTERS_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/filters";
  private static final Namespace FILTERS_NAMESPACE = getNamespace("filters", FILTERS_NAMESPACE_URI);

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri() = '" + FILTERS_NAMESPACE_URI + "' and local-name() = 'filter-by-ip-range']";

  @Override
  public String getDescription() {
    return "Update filter-by-ip-range filter to use the validations module.";
  }

  public ByIpRangeFilter() {
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

    final String ipFilterListName = getContainerElement(element).getAttributeValue("name") + "_filter-by-ip-range";
    element.setAttribute("whiteList", ipFilterListName);

    final Element ipFilterList = new Element("ip-filter-list", VALIDATION_NAMESPACE)
        .setAttribute("name", ipFilterListName);
    final Element ips = new Element("ips", VALIDATION_NAMESPACE);

    final SubnetUtils calculatedIp = new SubnetUtils(element.getAttributeValue("net"), element.getAttributeValue("mask"));
    ipFilterList.addContent(ips.addContent(new Element("ip", VALIDATION_NAMESPACE)
        .setAttribute("value", calculatedIp.getInfo().getCidrSignature())));

    addTopLevelElement(ipFilterList, element.getDocument());

    element.removeAttribute("net");
    element.removeAttribute("mask");

    handleFilter(element);
  }

}
