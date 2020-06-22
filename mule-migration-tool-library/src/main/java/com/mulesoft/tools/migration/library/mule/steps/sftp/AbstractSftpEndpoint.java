/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.sftp;

import static com.mulesoft.tools.migration.library.mule.steps.sftp.SftpConfig.SFTP_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getContainerElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateReconnection;
import static org.apache.commons.lang3.StringUtils.substring;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;

import java.util.Optional;

/**
 * Support for the migration of endpoints of the sftp transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractSftpEndpoint extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  protected static final String SFTP_NS_PREFIX = "sftp";
  protected static final String SFTP_NS_URI = "http://www.mulesoft.org/schema/mule/sftp";

  private ExpressionMigrator expressionMigrator;

  protected Element migrateSftpConfig(Element object, String configName, Optional<Element> config, MigrationReport report) {
    Element sftpConfig = config.orElseGet(() -> {
      Element sftpCfg = new Element("config", SFTP_NAMESPACE);
      sftpCfg.setAttribute("name", configName != null
          ? configName
          : (object.getAttributeValue("name") != null
              ? object.getAttributeValue("name")
              : (getContainerElement(object).getAttributeValue("name") + "Sftp"))
              + "Config");
      Element conn = new Element("connection", SFTP_NAMESPACE);
      migrateReconnection(conn, object, report);
      sftpCfg.addContent(conn);

      addTopLevelElement(sftpCfg, object.getDocument());

      return sftpCfg;
    });
    return sftpConfig;
  }

  protected String resolveDirectory(String endpointPath) {
    if (endpointPath.equals("/~")) {
      return "~";
    } else if (endpointPath.startsWith("/~/")) {
      return substring(endpointPath, 3);
    } else if (endpointPath.startsWith("/")) {
      return substring(endpointPath, 1);
    } else {
      return endpointPath;
    }
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
