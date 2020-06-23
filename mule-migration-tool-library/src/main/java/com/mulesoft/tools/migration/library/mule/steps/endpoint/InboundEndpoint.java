/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.endpoint;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addMigrationAttributeToElement;

import com.mulesoft.tools.migration.library.mule.steps.email.ImapInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.email.ImapsInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.email.Pop3InboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.email.Pop3sInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.file.FileInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.ftp.FtpEeInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpsInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.jms.JmsInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.sftp.SftpInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.vm.VmInboundEndpoint;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the generic inbound endpoints.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class InboundEndpoint extends AbstractApplicationModelMigrationStep
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
  public static final String XPATH_SELECTOR = "//mule:inbound-endpoint";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update generic inbound endpoints.";
  }

  public InboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.getChildren("property", CORE_NAMESPACE).forEach(p -> {
      object.setAttribute(p.getAttributeValue("key"), p.getAttributeValue("value"));
    });
    object.removeChildren("property", CORE_NAMESPACE);

    addMigrationAttributeToElement(object, new Attribute("isMessageSource", "true"));

    AbstractApplicationModelMigrationStep migrator = null;

    if (object.getAttribute("address") != null) {
      String address = object.getAttributeValue("address");

      // TODO MMT-132 make available migrators discoverable
      if (address.startsWith("file://")) {
        migrator = new FileInboundEndpoint();
        object.setNamespace(Namespace.getNamespace(FILE_NS_PREFIX, FILE_NS_URI));
      } else if (address.startsWith("ftp://")) {
        migrator = new FtpEeInboundEndpoint();
        object.setNamespace(Namespace.getNamespace(FTP_NS_PREFIX, FTP_NS_URI));
      } else if (address.startsWith("sftp://")) {
        migrator = new SftpInboundEndpoint();
        object.setNamespace(Namespace.getNamespace(SFTP_NS_PREFIX, SFTP_NS_URI));
      } else if (address.startsWith("http://")) {
        migrator = new HttpInboundEndpoint();
        object.setNamespace(Namespace.getNamespace(HTTP_NS_PREFIX, HTTP_NS_URI));
      } else if (address.startsWith("https://")) {
        migrator = new HttpsInboundEndpoint();
        object.setNamespace(Namespace.getNamespace("https", "http://www.mulesoft.org/schema/mule/https"));
      } else if (address.startsWith("imap://")) {
        migrator = new ImapInboundEndpoint();
        object.setNamespace(Namespace.getNamespace("imap", "http://www.mulesoft.org/schema/mule/imap"));
      } else if (address.startsWith("imaps://")) {
        migrator = new ImapsInboundEndpoint();
        object.setNamespace(Namespace.getNamespace("imaps", "http://www.mulesoft.org/schema/mule/imaps"));
      } else if (address.startsWith("pop3://")) {
        migrator = new Pop3InboundEndpoint();
        object.setNamespace(Namespace.getNamespace("pop3", "http://www.mulesoft.org/schema/mule/pop3"));
      } else if (address.startsWith("pop3s://")) {
        migrator = new Pop3sInboundEndpoint();
        object.setNamespace(Namespace.getNamespace("pop3s", "http://www.mulesoft.org/schema/mule/pop3s"));
      } else if (address.startsWith("jms://")) {
        migrator = new JmsInboundEndpoint();
        object.setNamespace(Namespace.getNamespace(JMS_NS_PREFIX, JMS_NS_URI));
      } else if (address.startsWith("vm://")) {
        migrator = new VmInboundEndpoint();
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
    } else if (object.getAttribute("ref") != null) {
      Element globalEndpoint = getApplicationModel().getNode("/*/*[@name = '" + object.getAttributeValue("ref") + "']");

      // TODO MMT-132 make available migrators discoverable
      if (globalEndpoint.getAttribute("address") != null) {
        String address = globalEndpoint.getAttributeValue("address");
        if (address.startsWith("file://")) {
          migrator = new FileInboundEndpoint();
          object.setNamespace(Namespace.getNamespace(FILE_NS_PREFIX, FILE_NS_URI));
        } else if (address.startsWith("ftp://")) {
          migrator = new FtpEeInboundEndpoint();
          object.setNamespace(Namespace.getNamespace(FTP_NS_PREFIX, FTP_NS_URI));
        } else if (address.startsWith("sftp://")) {
          migrator = new SftpInboundEndpoint();
          object.setNamespace(Namespace.getNamespace(SFTP_NS_PREFIX, SFTP_NS_URI));
        } else if (address.startsWith("http://")) {
          migrator = new HttpInboundEndpoint();
          object.setNamespace(Namespace.getNamespace(HTTP_NS_PREFIX, HTTP_NS_URI));
        } else if (address.startsWith("https://")) {
          migrator = new HttpsInboundEndpoint();
          object.setNamespace(Namespace.getNamespace("https", "http://www.mulesoft.org/schema/mule/https"));
        } else if (address.startsWith("imap://")) {
          migrator = new ImapInboundEndpoint();
          object.setNamespace(Namespace.getNamespace("imap", "http://www.mulesoft.org/schema/mule/imap"));
        } else if (address.startsWith("imaps://")) {
          migrator = new ImapsInboundEndpoint();
          object.setNamespace(Namespace.getNamespace("imaps", "http://www.mulesoft.org/schema/mule/imaps"));
        } else if (address.startsWith("pop3://")) {
          migrator = new Pop3InboundEndpoint();
          object.setNamespace(Namespace.getNamespace("pop3", "http://www.mulesoft.org/schema/mule/pop3"));
        } else if (address.startsWith("pop3s://")) {
          migrator = new Pop3sInboundEndpoint();
          object.setNamespace(Namespace.getNamespace("pop3s", "http://www.mulesoft.org/schema/mule/pop3s"));
        } else if (address.startsWith("jms://")) {
          migrator = new JmsInboundEndpoint();
          object.setNamespace(Namespace.getNamespace(JMS_NS_PREFIX, JMS_NS_URI));
        } else if (address.startsWith("vm://")) {
          migrator = new VmInboundEndpoint();
          object.setNamespace(Namespace.getNamespace(VM_NS_PREFIX, VM_NS_URI));
        }

        if (migrator != null) {
          migrator.setApplicationModel(getApplicationModel());
          if (migrator instanceof ExpressionMigratorAware) {
            ((ExpressionMigratorAware) migrator).setExpressionMigrator(getExpressionMigrator());
          }

          for (Attribute attribute : globalEndpoint.getAttributes()) {
            if (object.getAttribute(attribute.getName()) == null) {
              object.setAttribute(attribute.getName(), attribute.getValue());
            }
          }

          migrator.execute(object, report);
        }
      }
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
