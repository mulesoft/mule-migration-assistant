/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.endpoint;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;

import com.mulesoft.tools.migration.library.mule.steps.email.SmtpOutboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.email.SmtpsOutboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.file.FileOutboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.ftp.FtpEeOutboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpOutboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpsOutboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.jms.JmsOutboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.sftp.SftpOutboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.vm.VmOutboundEndpoint;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the generic outbound endpoints.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class OutboundEndpoint extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  private static final String HTTP_NS_PREFIX = "http";
  private static final String HTTP_NS_URI = "http://www.mulesoft.org/schema/mule/http";
  private static final String FILE_NS_PREFIX = "file";
  private static final String FILE_NS_URI = "http://www.mulesoft.org/schema/mule/file";
  private static final String FTP_NS_PREFIX = "ftp";
  private static final String FTP_NS_URI = "http://www.mulesoft.org/schema/mule/ee/ftp";
  private static final String SFTP_NS_PREFIX = "sftp";
  private static final String SFTP_NS_URI = "http://www.mulesoft.org/schema/mule/sftp";
  private static final String JMS_NS_PREFIX = "jms";
  private static final String JMS_NS_URI = "http://www.mulesoft.org/schema/mule/jms";
  private static final String VM_NS_PREFIX = "vm";
  private static final String VM_NS_URI = "http://www.mulesoft.org/schema/mule/vm";
  public static final String XPATH_SELECTOR = "//mule:outbound-endpoint";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update generic outbound endpoints.";
  }

  public OutboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.getChildren("property", CORE_NAMESPACE).forEach(p -> {
      object.setAttribute(p.getAttributeValue("key"), p.getAttributeValue("value"));
    });
    object.removeChildren("property", CORE_NAMESPACE);

    if (object.getAttribute("address") != null) {
      String address = object.getAttributeValue("address");

      AbstractApplicationModelMigrationStep migrator = null;
      // TODO MMT-132 make available migrators discoverable
      if (address.startsWith("file://")) {
        migrator = new FileOutboundEndpoint();
        object.setNamespace(Namespace.getNamespace(FILE_NS_PREFIX, FILE_NS_URI));
      } else if (address.startsWith("ftp://")) {
        migrator = new FtpEeOutboundEndpoint();
        object.setNamespace(Namespace.getNamespace(FTP_NS_PREFIX, FTP_NS_URI));
      } else if (address.startsWith("sftp://")) {
        migrator = new SftpOutboundEndpoint();
        object.setNamespace(Namespace.getNamespace(SFTP_NS_PREFIX, SFTP_NS_URI));
      } else if (address.startsWith("http://")) {
        migrator = new HttpOutboundEndpoint();
        object.setNamespace(Namespace.getNamespace(HTTP_NS_PREFIX, HTTP_NS_URI));
      } else if (address.startsWith("https://")) {
        migrator = new HttpsOutboundEndpoint();
        object.setNamespace(Namespace.getNamespace("https", "http://www.mulesoft.org/schema/mule/https"));
      } else if (address.startsWith("smtp://")) {
        migrator = new SmtpOutboundEndpoint();
        object.setNamespace(Namespace.getNamespace("smtp", "http://www.mulesoft.org/schema/mule/smtp"));
      } else if (address.startsWith("smtps://")) {
        migrator = new SmtpsOutboundEndpoint();
        object.setNamespace(Namespace.getNamespace("smtps", "http://www.mulesoft.org/schema/mule/smtps"));
      } else if (address.startsWith("jms://")) {
        migrator = new JmsOutboundEndpoint();
        object.setNamespace(Namespace.getNamespace(JMS_NS_PREFIX, JMS_NS_URI));
      } else if (address.startsWith("vm://")) {
        migrator = new VmOutboundEndpoint();
        object.setNamespace(Namespace.getNamespace(VM_NS_PREFIX, VM_NS_URI));
      }

      if (migrator != null) {
        migrator.setApplicationModel(getApplicationModel());
        if (migrator instanceof ExpressionMigratorAware) {
          ((ExpressionMigratorAware) migrator).setExpressionMigrator(getExpressionMigrator());
        }

        migrator.execute(object, report);
      }
      object.removeAttribute("address");
    }

    if (object.getAttribute("exchange-pattern") != null) {
      object.removeAttribute("exchange-pattern");
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
