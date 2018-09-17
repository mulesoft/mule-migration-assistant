/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.ftp;

import static com.mulesoft.tools.migration.library.mule.steps.ftp.FtpConfig.FTP_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;

import java.util.Optional;

public abstract class AbstractFtpEndpoint extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  protected static final String FTP_NS_PREFIX = "ftp";
  protected static final String FTP_NS_URI = "http://www.mulesoft.org/schema/mule/ftp";

  private ExpressionMigrator expressionMigrator;

  protected Element migrateFtpConfig(Element object, String configName, Optional<Element> config) {
    Element ftpConfig = config.orElseGet(() -> {
      Element ftpCfg = new Element("config", FTP_NAMESPACE);
      ftpCfg.setAttribute("name", configName != null ? configName : object.getAttributeValue("name") + "Config");
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
