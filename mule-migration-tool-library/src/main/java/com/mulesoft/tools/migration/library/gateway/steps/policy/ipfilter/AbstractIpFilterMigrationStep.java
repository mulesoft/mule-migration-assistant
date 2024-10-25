/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.ipfilter;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.IP_FILTER_NAMESPACE;

import com.mulesoft.tools.migration.library.gateway.steps.policy.PolicyMigrationStep;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Common stuff to migrate ip filters
 *
 * @author Mulesoft Inc.
 */
public abstract class AbstractIpFilterMigrationStep extends PolicyMigrationStep {

  protected static final String CONFIG_TAG_NAME = "config";
  protected static final String NAME_TAG_NAME = "name";
  protected static final String IPS_TAG_NAME = "ips";

  protected static final String IP_ADDRESS_ATTR_NAME = "ipAddress";

  public AbstractIpFilterMigrationStep(final Namespace namespace, final String tagName) {
    super(namespace, tagName);
  }

  protected AbstractIpFilterMigrationStep() {
    super();
  }

  protected Element getConfigElementFromDocument(Element root) {
    return root.getChild(CONFIG_TAG_NAME, IP_FILTER_NAMESPACE);
  }

  protected boolean hasConfigTag(Element root) {
    return root != null && getConfigElementFromDocument(root) != null;
  }

  private Attribute getIpAddressAttr(Element root) {
    return getConfigElementFromDocument(root).getAttribute(IP_ADDRESS_ATTR_NAME).detach();
  }

  protected void setIpAddressAttribute(Element element) {
    Element root = getRootElement(element);
    if (hasConfigTag(root)) {
      element.setAttribute(getIpAddressAttr(root));
    }
  }

  private void setConfigNameAttribute(Element element, final String configRefAttrValue) {
    Element root = getRootElement(element);
    if (hasConfigTag(root)) {
      getConfigElementFromDocument(root).setAttribute(new Attribute(NAME_TAG_NAME, configRefAttrValue));
    }
  }

  protected void moveBlacklistWhitelistContent(Element element) {
    List<Content> content = detachContent(element.getContent());
    Element root = getRootElement(element);
    if (hasConfigTag(root)) {
      getConfigElementFromDocument(root).getChild(IPS_TAG_NAME, IP_FILTER_NAMESPACE).addContent(content);
    }
  }

  protected void migrateBlacklistWhitelistElement(Element element, final String configRefAttrValue) {
    element.setNamespace(IP_FILTER_NAMESPACE);
    element.setAttribute(new Attribute(CONFIG_REF_ATTR_NAME, configRefAttrValue));
    setIpAddressAttribute(element);
    setConfigNameAttribute(element, configRefAttrValue);
    moveBlacklistWhitelistContent(element);
  }
}
