/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.salesforce;

import com.mulesoft.tools.migration.library.tools.SalesforceUtils;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.util.XmlDslUtils;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.project.model.ApplicationModel.addNameSpace;

/**
 * Migrate Cached Basic configuration
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CachedBasicConfiguration extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  private static final String MULE3_NAME = "cached-basic-config";
  private static final String MULE4_CONFIG = "sfdc-config";
  private static final String MULE4_NAME = "basic-connection";
  private static final String MULE4_PROXY = "proxy-configuration";

  private ExpressionMigrator expressionMigrator;

  public CachedBasicConfiguration() {
    this.setAppliedTo(XmlDslUtils.getXPathSelector(SalesforceUtils.MULE3_SALESFORCE_NAMESPACE_URI, MULE3_NAME, false));
    this.setNamespacesContributions(newArrayList(SalesforceUtils.MULE3_SALESFORCE_NAMESPACE));
  }

  @Override
  public void execute(Element mule3CachedBasicConfig, MigrationReport report) throws RuntimeException {
    addNameSpace(SalesforceUtils.MULE4_SALESFORCE_NAMESPACE,
                 SalesforceUtils.MULE4_SALESFORCE_NAMESPACE_URI, mule3CachedBasicConfig.getDocument());

    Element mule4Config = new Element(MULE4_CONFIG, SalesforceUtils.MULE4_SALESFORCE_NAMESPACE);

    setConfigAttributes(mule3CachedBasicConfig, mule4Config);
    setConnectionAttributes(mule3CachedBasicConfig, mule4Config);

    Optional<Element> mule3ApexConfiguration =
        Optional.ofNullable(mule3CachedBasicConfig.getChild("apex-class-names", SalesforceUtils.MULE3_SALESFORCE_NAMESPACE));
    mule3ApexConfiguration.ifPresent(apexClassNames -> {
      Element apexClassNameChild = apexClassNames.getChild("apex-class-name", SalesforceUtils.MULE3_SALESFORCE_NAMESPACE);
      String apexClassNameValue = apexClassNameChild.getText();
      if (apexClassNameValue != null) {
        Element mule4ApexClassNames = new Element("apex-class-names", SalesforceUtils.MULE4_SALESFORCE_NAMESPACE);
        Element mule4ApexClassNameChild = new Element("apex-class-name", SalesforceUtils.MULE4_SALESFORCE_NAMESPACE);
        mule4ApexClassNameChild.setAttribute("value", apexClassNameValue);
        mule4ApexClassNames.addContent(mule4ApexClassNameChild);
        mule4Config.addContent(mule4ApexClassNames);
      }
    });

    XmlDslUtils.addElementAfter(mule4Config, mule3CachedBasicConfig);
    mule3CachedBasicConfig.getParentElement().removeContent(mule3CachedBasicConfig);
  }

  private void setConfigAttributes(Element mule3CachedBasicConfig, Element mule4Config) {
    String nameValue = mule3CachedBasicConfig.getAttributeValue("name");
    if (nameValue != null) {
      mule4Config.setAttribute("name", nameValue);
    }

    String docName = mule3CachedBasicConfig.getAttributeValue("name", SalesforceUtils.DOC_NAMESPACE);
    if (docName != null) {
      mule4Config.setAttribute("name", docName, SalesforceUtils.DOC_NAMESPACE);
    }

    String fetchAllApexSoapMetadataValue = mule3CachedBasicConfig.getAttributeValue("fetchAllApexSoapMetadata");
    if (fetchAllApexSoapMetadataValue != null) {
      mule4Config.setAttribute("fetchAllApexSoapMetadata", fetchAllApexSoapMetadataValue);
    }

    String fetchAllApexRestMetadataValue = mule3CachedBasicConfig.getAttributeValue("fetchAllApexRestMetadata");
    if (fetchAllApexRestMetadataValue != null) {
      mule4Config.setAttribute("fetchAllApexRestMetadata", fetchAllApexRestMetadataValue);
    }
  }

  private void setConnectionAttributes(Element mule3CachedBasicConfig, Element mule4Config) {
    Element mule4BasicConnection = new Element(MULE4_NAME, SalesforceUtils.MULE4_SALESFORCE_NAMESPACE);

    String usernameValue = mule3CachedBasicConfig.getAttributeValue("username");
    if (usernameValue != null) {
      mule4BasicConnection.setAttribute("username", usernameValue);
    }

    String passwordValue = mule3CachedBasicConfig.getAttributeValue("password");
    if (passwordValue != null) {
      mule4BasicConnection.setAttribute("password", passwordValue);
    }

    String securityTokenValue = mule3CachedBasicConfig.getAttributeValue("securityToken");
    if (securityTokenValue != null) {
      mule4BasicConnection.setAttribute("securityToken", securityTokenValue);
    }

    String readTimeoutValue = mule3CachedBasicConfig.getAttributeValue("readTimeout");
    if (readTimeoutValue != null) {
      mule4BasicConnection.setAttribute("readTimeout", readTimeoutValue);
    }

    String connectionTimeoutValue = mule3CachedBasicConfig.getAttributeValue("connectionTimeout");
    if (connectionTimeoutValue != null) {
      mule4BasicConnection.setAttribute("connectionTimeout", connectionTimeoutValue);
    }

    String assignmentRuleIdValue = mule3CachedBasicConfig.getAttributeValue("assignmentRuleId");
    if (assignmentRuleIdValue != null) {
      mule4BasicConnection.setAttribute("assignmentRuleId", assignmentRuleIdValue);
    }

    String clientIdValue = mule3CachedBasicConfig.getAttributeValue("clientId");
    if (clientIdValue != null) {
      mule4BasicConnection.setAttribute("clientId", clientIdValue);
    }

    String timeObjectStoreValue = mule3CachedBasicConfig.getAttributeValue("timeObjectStore-ref");
    if (timeObjectStoreValue != null) {
      String expression = expressionMigrator.migrateExpression(timeObjectStoreValue, true, mule3CachedBasicConfig);
      mule4BasicConnection.setAttribute("timeObjectStore", expression);
    }

    String sessionIdValue = mule3CachedBasicConfig.getAttributeValue("sessionId");
    if (sessionIdValue != null) {
      mule4BasicConnection.setAttribute("sessionId", sessionIdValue);
    }

    String serviceEndpointValue = mule3CachedBasicConfig.getAttributeValue("serviceEndpoint");
    if (serviceEndpointValue != null) {
      mule4BasicConnection.setAttribute("serviceEndpoint", serviceEndpointValue);
    }

    String allowFieldTruncationSupportValue = mule3CachedBasicConfig.getAttributeValue("allowFieldTruncationSupport");
    if (allowFieldTruncationSupportValue != null) {
      mule4BasicConnection.setAttribute("allowFieldTruncationSupport", allowFieldTruncationSupportValue);
    }

    String useDefaultRuleValue = mule3CachedBasicConfig.getAttributeValue("useDefaultRule");
    if (useDefaultRuleValue != null) {
      mule4BasicConnection.setAttribute("useDefaultRule", useDefaultRuleValue);
    }

    String clearNullFieldsValue = mule3CachedBasicConfig.getAttributeValue("clearNullFields");
    if (clearNullFieldsValue != null) {
      mule4BasicConnection.setAttribute("clearNullFields", clearNullFieldsValue);
    }

    setProxyConfiguration(mule3CachedBasicConfig, mule4BasicConnection);

    mule4Config.addContent(mule4BasicConnection);
  }

  private void setProxyConfiguration(Element mule3CachedBasicConfig, Element mule4BasicConnection) {
    String proxyHostValue = mule3CachedBasicConfig.getAttributeValue("proxyHost");
    if (proxyHostValue != null && !proxyHostValue.isEmpty()) {
      Element mule4ProxyBasicConfig = new Element(MULE4_PROXY, SalesforceUtils.MULE4_SALESFORCE_NAMESPACE);
      mule4ProxyBasicConfig.setAttribute("host", proxyHostValue);
      mule4ProxyBasicConfig.setAttribute("username", mule3CachedBasicConfig.getAttributeValue("proxyUsername"));
      mule4ProxyBasicConfig.setAttribute("password", mule3CachedBasicConfig.getAttributeValue("proxyPassword"));
      mule4ProxyBasicConfig.setAttribute("port", mule3CachedBasicConfig.getAttributeValue("proxyPort"));
      mule4BasicConnection.addContent(mule4ProxyBasicConfig);
    }

    Optional<Element> reconnectElement = Optional.ofNullable(mule3CachedBasicConfig
        .getChild("reconnect", Namespace.getNamespace("http://www.mulesoft.org/schema/mule/core")));
    reconnectElement.ifPresent(reconnect -> {
      Element mule4Reconnection = new Element("reconnection");
      Element mule4Reconnect = new Element("reconnect");

      String frequency = reconnect.getAttributeValue("frequency");
      if (frequency != null) {
        mule4Reconnect.setAttribute("frequency", frequency);
      }

      String count = reconnect.getAttributeValue("count");
      if (count != null) {
        mule4Reconnect.setAttribute("count", count);
      }

      mule4Reconnection.addContent(mule4Reconnect);
      mule4BasicConnection.addContent(mule4Reconnection);
    });
  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return this.expressionMigrator;
  }
}
