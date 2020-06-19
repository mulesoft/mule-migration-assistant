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
