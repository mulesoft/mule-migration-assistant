/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.ipfilter;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.IP_FILTER_GW_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.IP_FILTER_NAMESPACE;

import com.mulesoft.tools.migration.library.gateway.steps.policy.PolicyMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;

/**
 * Migrate ip element
 *
 * @author Mulesoft Inc.
 */
public class IpTagMigrationStep extends AbstractIpFilterMigrationStep {

  private static final String IP_TAG_NAME = "ip";

  public IpTagMigrationStep() {
    super(IP_FILTER_GW_NAMESPACE, IP_TAG_NAME);
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    element.setNamespace(IP_FILTER_NAMESPACE);
    final List<Content> content = detachContent(element.getContent());
    if (content.size() > 0) {
      element.setAttribute(new Attribute(PolicyMigrationStep.VALUE_ATTR_NAME, (content.get(0)).getValue()));
    }
  }
}
