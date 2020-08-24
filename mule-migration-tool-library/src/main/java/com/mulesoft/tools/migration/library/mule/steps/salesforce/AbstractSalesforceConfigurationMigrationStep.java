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

import static com.mulesoft.tools.migration.project.model.ApplicationModel.addNameSpace;

/**
 * Migrate Abstract Salesforce Application  Configuration Migration Step
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractSalesforceConfigurationMigrationStep extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  private final String name;
  protected ExpressionMigrator expressionMigrator;
  protected Element mule4Config;
  protected Element mule4Connection;
  private final String mule4Name;
  private static final String MULE4_PROXY = "proxy-configuration";

  public AbstractSalesforceConfigurationMigrationStep(String name, String mule4Name) {
    this.name = name;
    this.mule4Name = mule4Name;
  }

  @Override
  public void execute(Element mule3Config, MigrationReport report) throws RuntimeException {
    addNameSpace(SalesforceUtils.MULE4_SALESFORCE_NAMESPACE,
                 SalesforceUtils.MULE4_SALESFORCE_SCHEMA_LOCATION, mule3Config.getDocument());

    mule4Config = new Element(getName(), SalesforceUtils.MULE4_SALESFORCE_NAMESPACE);
    setDefaultAttributes(mule3Config, mule4Config, report);
    setDefaultConnectionAttributes(mule3Config, mule4Config, mule4Name);

    Optional<Element> mule3ApexConfiguration =
        Optional.ofNullable(mule3Config.getChild("apex-class-names", SalesforceUtils.MULE3_SALESFORCE_NAMESPACE));
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
  }

  private void setDefaultAttributes(Element mule3Config, Element mule4Config, MigrationReport report) {
    String nameValue = mule3Config.getAttributeValue("name");
    if (nameValue != null) {
      mule4Config.setAttribute("name", nameValue);
    }

    String docName = mule3Config.getAttributeValue("name", SalesforceUtils.DOC_NAMESPACE);
    if (docName != null) {
      mule4Config.setAttribute("name", docName, SalesforceUtils.DOC_NAMESPACE);
    }

    String fetchAllApexSoapMetadataValue = mule3Config.getAttributeValue("fetchAllApexSoapMetadata");
    if (fetchAllApexSoapMetadataValue != null) {
      mule4Config.setAttribute("fetchAllApexSoapMetadata", fetchAllApexSoapMetadataValue);
    }

    String fetchAllApexRestMetadataValue = mule3Config.getAttributeValue("fetchAllApexRestMetadata");
    if (fetchAllApexRestMetadataValue != null) {
      mule4Config.setAttribute("fetchAllApexRestMetadata", fetchAllApexRestMetadataValue);
    }
  }

  private void setDefaultConnectionAttributes(Element mule3Config, Element mule4Config, String mule4Name) {
    mule4Connection = new Element(mule4Name, SalesforceUtils.MULE4_SALESFORCE_NAMESPACE);

    String usernameValue = mule3Config.getAttributeValue("username");
    if (usernameValue != null) {
      mule4Connection.setAttribute("username", usernameValue);
    }

    String passwordValue = mule3Config.getAttributeValue("password");
    if (passwordValue != null) {
      mule4Connection.setAttribute("password", passwordValue);
    }

    String securityTokenValue = mule3Config.getAttributeValue("securityToken");
    if (securityTokenValue != null) {
      mule4Connection.setAttribute("securityToken", securityTokenValue);
    }

    String readTimeoutValue = mule3Config.getAttributeValue("readTimeout");
    if (readTimeoutValue != null) {
      mule4Connection.setAttribute("readTimeout", readTimeoutValue);
    }

    String connectionTimeoutValue = mule3Config.getAttributeValue("connectionTimeout");
    if (connectionTimeoutValue != null) {
      mule4Connection.setAttribute("connectionTimeout", connectionTimeoutValue);
    }

    String assignmentRuleIdValue = mule3Config.getAttributeValue("assignmentRuleId");
    if (assignmentRuleIdValue != null) {
      mule4Connection.setAttribute("assignmentRuleId", assignmentRuleIdValue);
    }

    String clientIdValue = mule3Config.getAttributeValue("clientId");
    if (clientIdValue != null) {
      mule4Connection.setAttribute("clientId", clientIdValue);
    }

    String timeObjectStoreValue = mule3Config.getAttributeValue("timeObjectStore-ref");
    if (timeObjectStoreValue != null) {
      String expression = expressionMigrator.migrateExpression(timeObjectStoreValue, true, mule3Config);
      mule4Connection.setAttribute("timeObjectStore", expression);
    }

    String sessionIdValue = mule3Config.getAttributeValue("sessionId");
    if (sessionIdValue != null) {
      mule4Connection.setAttribute("sessionId", sessionIdValue);
    }

    String serviceEndpointValue = mule3Config.getAttributeValue("serviceEndpoint");
    if (serviceEndpointValue != null) {
      mule4Connection.setAttribute("serviceEndpoint", serviceEndpointValue);
    }

    String allowFieldTruncationSupportValue = mule3Config.getAttributeValue("allowFieldTruncationSupport");
    if (allowFieldTruncationSupportValue != null) {
      mule4Connection.setAttribute("allowFieldTruncationSupport", allowFieldTruncationSupportValue);
    }

    String useDefaultRuleValue = mule3Config.getAttributeValue("useDefaultRule");
    if (useDefaultRuleValue != null) {
      mule4Connection.setAttribute("useDefaultRule", useDefaultRuleValue);
    }

    String clearNullFieldsValue = mule3Config.getAttributeValue("clearNullFields");
    if (clearNullFieldsValue != null) {
      mule4Connection.setAttribute("clearNullFields", clearNullFieldsValue);
    }

    String consumerKey = mule3Config.getAttributeValue("consumerKey");
    if (consumerKey != null) {
      mule4Connection.setAttribute("consumerKey", consumerKey);
    }

    String consumerSecret = mule3Config.getAttributeValue("consumerSecret");
    if (consumerSecret != null) {
      mule4Connection.setAttribute("consumerSecret", consumerSecret);
    }

    String apiVersion = mule3Config.getAttributeValue("apiVersion");
    if (apiVersion != null) {
      mule4Connection.setAttribute("apiVersion", apiVersion);
    }

    String keyStore = mule3Config.getAttributeValue("keyStore");
    if (keyStore != null) {
      mule4Connection.setAttribute("keyStore", keyStore);
    }

    String storePassword = mule3Config.getAttributeValue("storePassword");
    if (storePassword != null) {
      mule4Connection.setAttribute("storePassword", storePassword);
    }

    String principal = mule3Config.getAttributeValue("principal");
    if (principal != null) {
      mule4Connection.setAttribute("principal", principal);
    }
    
    setProxyConfiguration(mule3Config, mule4Connection);
    mule4Config.addContent(mule4Connection);

  }

  private void setProxyConfiguration(Element mule3Config, Element mule4Connection) {
    String proxyHostValue = mule3Config.getAttributeValue("proxyHost");
    if (proxyHostValue != null && !proxyHostValue.isEmpty()) {
      Element mule4ProxyBasicConfig = new Element(MULE4_PROXY, SalesforceUtils.MULE4_SALESFORCE_NAMESPACE);
      mule4ProxyBasicConfig.setAttribute("host", proxyHostValue);
      mule4ProxyBasicConfig.setAttribute("username", mule3Config.getAttributeValue("proxyUsername"));
      mule4ProxyBasicConfig.setAttribute("password", mule3Config.getAttributeValue("proxyPassword"));
      mule4ProxyBasicConfig.setAttribute("port", mule3Config.getAttributeValue("proxyPort"));
      mule4Connection.addContent(mule4ProxyBasicConfig);
    }

    Optional<Element> reconnectElement = Optional.ofNullable(mule3Config
        .getChild("reconnect", Namespace.getNamespace(XmlDslUtils.CORE_NS_URI)));

    Optional<Element> reconnectForeverElement = Optional.ofNullable(mule3Config
        .getChild("reconnect-forever", Namespace.getNamespace(XmlDslUtils.CORE_NS_URI)));

    if (reconnectElement.isPresent()) {
      migrateReconnection(mule4Connection, reconnectElement, "reconnect");
    } else if (reconnectForeverElement.isPresent()) {
      migrateReconnection(mule4Connection, reconnectForeverElement, "reconnect-forever");
    }
  }

  private void migrateReconnection(Element mule4Connection, Optional<Element> reconnectElement, String elementName) {
    reconnectElement.ifPresent(reconnect -> {
      Element mule4Reconnection = new Element("reconnection");
      Element mule4Reconnect = new Element(elementName);

      String frequency = reconnect.getAttributeValue("frequency");
      if (frequency != null) {
        mule4Reconnect.setAttribute("frequency", frequency);
      }

      String count = reconnect.getAttributeValue("count");
      if (count != null) {
        mule4Reconnect.setAttribute("count", count);
      }

      mule4Reconnection.addContent(mule4Reconnect);
      mule4Connection.addContent(mule4Reconnection);
    });
  }

  public String getName() {
    return name;
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
