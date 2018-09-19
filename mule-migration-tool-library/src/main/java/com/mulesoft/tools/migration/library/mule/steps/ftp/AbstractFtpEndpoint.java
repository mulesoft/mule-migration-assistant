/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.ftp;

import static com.mulesoft.tools.migration.library.mule.steps.ftp.FtpConfig.FTP_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getFlow;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;

import java.util.Optional;

/**
 * Support for the migration of endpoints of the ftp transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractFtpEndpoint extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  protected static final String FTP_NS_PREFIX = "ftp";
  protected static final String FTP_NS_URI = "http://www.mulesoft.org/schema/mule/ftp";

  private ExpressionMigrator expressionMigrator;

  protected Element migrateFtpConfig(Element object, String configName, Optional<Element> config) {
    Element ftpConfig = config.orElseGet(() -> {
      Element ftpCfg = new Element("config", FTP_NAMESPACE);
      ftpCfg.setAttribute("name", configName != null
          ? configName
          : (object.getAttributeValue("name") != null
              ? object.getAttributeValue("name")
              : (getFlow(object).getAttributeValue("name") + "Ftp"))
              + "Config");
      Element conn = new Element("connection", FTP_NAMESPACE);
      conn.addContent(new Element("reconnection", CORE_NAMESPACE).setAttribute("failsDeployment", "true"));
      ftpCfg.addContent(conn);

      addTopLevelElement(ftpCfg, object.getDocument());

      return ftpCfg;
    });
    return ftpConfig;
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
