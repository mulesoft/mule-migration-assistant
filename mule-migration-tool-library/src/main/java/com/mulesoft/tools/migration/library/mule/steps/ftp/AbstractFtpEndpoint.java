/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.ftp;

import static com.mulesoft.tools.migration.library.mule.steps.ftp.FtpConfig.FTP_NAMESPACE;
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

  protected Element migrateFtpConfig(Element object, Optional<Element> m3Config, String configName, Optional<Element> config,
                                     MigrationReport report) {
    Element ftpConfig = config.orElseGet(() -> {
      Element ftpCfg = new Element("config", FTP_NAMESPACE);
      ftpCfg.setAttribute("name", configName != null
          ? configName
          : (object.getAttributeValue("name") != null
              ? object.getAttributeValue("name")
              : (getContainerElement(object).getAttributeValue("name") + "Ftp"))
              + "Config");
      Element conn = new Element("connection", FTP_NAMESPACE);
      migrateReconnection(conn, object, report);
      ftpCfg.addContent(conn);

      addTopLevelElement(ftpCfg, object.getDocument());

      return ftpCfg;
    });
    return ftpConfig;
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
